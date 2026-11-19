"""Receiver-type-aware call resolution (Java) and dispatch-aware impact.

The fixture reproduces what the factory measured: same-named methods on several classes (`getDto`), calls
through a chain, `var`, a Spring Data repository whose `save` is a library method next to a repository class
with its own `save`, implicit `this`, a static call, and a port interface called through a field.
"""

import pytest

from repolens.config import Config, Source
from repolens.service import KnowledgeGraph

FILES = {
    "src/a/Folio.java": """package a;
public class Folio {
    public DtoFolio getDto() { return new DtoFolio(); }
    public static Folio make() { return new Folio(); }
}
""",
    "src/a/Deed.java": """package a;
public class Deed {
    private Folio folio;
    public DtoDeed getDto() { return new DtoDeed(); }
    public Folio getFolio() { return folio; }
}
""",
    "src/a/DtoFolio.java": "package a;\npublic class DtoFolio {\n    public String getName() { return \"\"; }\n}\n",
    "src/a/DtoDeed.java": "package a;\npublic class DtoDeed {\n    public String getName() { return \"\"; }\n}\n",
    "src/a/FolioRepo.java": """package a;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FolioRepo extends JpaRepository<Folio, Integer> {
    Folio findByCode(String code);
}
""",
    "src/a/FolioStore.java": "package a;\npublic class FolioStore {\n    public Folio save(Folio f) { return f; }\n}\n",
    "src/a/Port.java": "package a;\npublic interface Port {\n    void apply(String value);\n}\n",
    "src/a/PortImpl.java": """package a;
public class PortImpl implements Port {
    @Override
    public void apply(String value) { }
}
""",
    "src/b/Service.java": """package b;
import a.Deed;
import a.Folio;
import a.FolioRepo;
import a.Port;
import java.util.List;
import java.util.Optional;
public class Service {
    private final Deed deed = new Deed();
    private FolioRepo repo;
    private Port port;

    public String onlyFolio(Folio folio) {
        return folio.getDto().getName();
    }

    public void onlyDeed() {
        deed.getDto();
    }

    public void chained(Deed source) {
        source.getFolio().getDto();
    }

    public void inferred() {
        var other = new Deed();
        other.getDto();
    }

    public void library(Folio folio) {
        repo.save(folio);
        repo.findByCode("x");
    }

    public void local() {
        helper();
        Folio.make();
        port.apply("v");
    }

    void helper() { }

    public void generics(List<Folio> folios, Optional<Deed> maybe) {
        folios.get(0).getDto();
        maybe.orElseThrow().getFolio();
        for (var f : folios) {
            f.getDto();
        }
        folios.forEach(each -> each.getDto());
        folios.stream().map(Folio::getDto);
    }

    public void repository() {
        repo.findById(1).orElseThrow().getDto();
        repo.save(Folio.make()).getDto();
    }
}
""",
}


@pytest.fixture(scope="module")
def kg(tmp_path_factory):
    root = tmp_path_factory.mktemp("typed")
    for rel, text in FILES.items():
        path = root / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8")
    cfg = Config(db_path=root / "g.db", sources=[Source("p", root)])
    cfg.embedder, cfg.workers, cfg.git_commits = "none", 1, 0
    graph = KnowledgeGraph(cfg)
    graph.index(log=lambda *_: None)
    yield graph
    graph.close()


def calls_from(kg, method: str) -> set[str]:
    src = f"p:src/b/Service.java#Service.{method}"
    return {d.split("#", 1)[1] for s, d, k, _ in kg.store.edges() if s == src and k == "calls"}


def test_the_receiver_type_picks_the_method_among_same_named_ones(kg):
    assert calls_from(kg, "onlyFolio") == {"Folio.getDto", "DtoFolio.getName"}
    assert calls_from(kg, "onlyDeed") == {"Deed.getDto"}


def test_chained_calls_follow_return_types(kg):
    assert calls_from(kg, "chained") == {"Deed.getFolio", "Folio.getDto"}


def test_var_takes_the_type_of_its_initializer(kg):
    assert calls_from(kg, "inferred") == {"Deed.getDto"}


def test_a_library_method_is_not_linked_to_a_same_named_repository_method(kg):
    assert calls_from(kg, "library") == {"FolioRepo.findByCode"}


def test_implicit_this_static_and_interface_calls(kg):
    assert calls_from(kg, "local") == {"Service.helper", "Folio.make", "Port.apply"}


def test_generic_elements_lambdas_and_method_references(kg):
    assert calls_from(kg, "generics") == {"Folio.getDto", "Deed.getFolio"}


def test_spring_data_methods_return_the_repository_entity(kg):
    assert calls_from(kg, "repository") == {"Folio.getDto", "Folio.make"}


def test_impact_of_an_implementation_includes_callers_through_its_interface(kg):
    result = kg.engine.impact("PortImpl.apply", depth=1)
    assert "src/b/Service.java" in result["files"]


def test_impact_of_a_method_names_only_its_real_callers(kg):
    folio = kg.engine.impact("Folio.getDto", depth=1)
    deed = kg.engine.impact("Deed.getDto", depth=1)
    folio_callers = {a["id"].split("#", 1)[1] for a in folio["affected"] if a["via"] == "calls"}
    deed_callers = {a["id"].split("#", 1)[1] for a in deed["affected"] if a["via"] == "calls"}
    assert folio_callers == {"Service.onlyFolio", "Service.chained", "Service.generics", "Service.repository"}
    assert deed_callers == {"Service.onlyDeed", "Service.inferred"}

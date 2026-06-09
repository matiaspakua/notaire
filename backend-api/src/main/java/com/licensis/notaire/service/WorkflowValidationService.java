package com.licensis.notaire.service;

import com.licensis.notaire.negocio.WorkflowNode;
import com.licensis.notaire.negocio.WorkflowNodeType;
import com.licensis.notaire.negocio.WorkflowTransition;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WorkflowValidationService {

    public ValidationResult validate(List<WorkflowNode> nodes, List<WorkflowTransition> transitions) {
        List<String> errors = new ArrayList<>();

        if (nodes.isEmpty()) {
            errors.add("El workflow no tiene nodos.");
            return ValidationResult.invalid(errors);
        }

        long initialCount = nodes.stream().filter(n -> n.getTipo() == WorkflowNodeType.INITIAL).count();
        if (initialCount == 0) {
            errors.add("El workflow debe tener exactamente un nodo inicial.");
        } else if (initialCount > 1) {
            errors.add("El workflow tiene más de un nodo inicial (" + initialCount + ").");
        }

        long finalCount = nodes.stream().filter(n -> n.getTipo() == WorkflowNodeType.FINAL).count();
        if (finalCount == 0) {
            errors.add("El workflow debe tener al menos un nodo final.");
        }

        if (!errors.isEmpty()) {
            return ValidationResult.invalid(errors);
        }

        Set<Integer> reachable = reachableFrom(
                nodes.stream().filter(n -> n.getTipo() == WorkflowNodeType.INITIAL)
                        .findFirst().map(WorkflowNode::getId).orElse(-1),
                transitions);

        Set<Integer> allIds = nodes.stream().map(WorkflowNode::getId).collect(Collectors.toSet());
        Set<Integer> unreachable = new HashSet<>(allIds);
        unreachable.removeAll(reachable);

        if (!unreachable.isEmpty()) {
            String names = nodes.stream()
                    .filter(n -> unreachable.contains(n.getId()))
                    .map(n -> n.getEstadoDeGestion() != null ? n.getEstadoDeGestion().getNombre() : String.valueOf(n.getId()))
                    .collect(Collectors.joining(", "));
            errors.add("Nodos no alcanzables desde el nodo inicial (aislados): " + names + ".");
        }

        return errors.isEmpty() ? ValidationResult.valid() : ValidationResult.invalid(errors);
    }

    private Set<Integer> reachableFrom(Integer startId, List<WorkflowTransition> transitions) {
        Map<Integer, List<Integer>> adjacency = transitions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getNodoOrigen().getId(),
                        Collectors.mapping(t -> t.getNodoDestino().getId(), Collectors.toList())));

        Set<Integer> visited = new HashSet<>();
        dfs(startId, adjacency, visited);
        return visited;
    }

    private void dfs(Integer node, Map<Integer, List<Integer>> adjacency, Set<Integer> visited) {
        if (visited.contains(node)) {
            return;
        }
        visited.add(node);
        for (Integer neighbour : adjacency.getOrDefault(node, List.of())) {
            dfs(neighbour, adjacency, visited);
        }
    }

    public static final class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        private ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, List.of());
        }

        public static ValidationResult invalid(List<String> errors) {
            return new ValidationResult(false, errors);
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}

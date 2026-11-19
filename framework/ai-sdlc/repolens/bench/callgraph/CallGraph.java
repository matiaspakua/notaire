import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Ground truth for the caller benchmark: every method invocation and method reference in the given source
 * roots, resolved by javac to the method it statically binds to. One tab-separated line per call:
 * caller file, caller line, callee owner (binary name), callee method, callee declaring file (empty when the
 * callee is not in these sources, e.g. a library). Usage: java CallGraph.java out.tsv classpath root...
 */
public final class CallGraph {

    private CallGraph() {
    }

    public static void main(String[] args) throws Exception {
        List<Path> files = new ArrayList<>();
        for (int i = 2; i < args.length; i++) {
            try (Stream<Path> walk = Files.walk(Path.of(args[i]))) {
                walk.filter(p -> p.toString().endsWith(".java")).forEach(files::add);
            }
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fm = compiler.getStandardFileManager(null, null, null);
        List<String> options = List.of("-cp", args[1], "-proc:full", "-Xlint:none", "-nowarn", "-XDshould-stop.at=FLOW");
        JavacTask task = (JavacTask) compiler.getTask(null, fm, d -> { }, options, null,
                fm.getJavaFileObjectsFromPaths(files));
        Iterable<? extends CompilationUnitTree> units = task.parse();
        task.analyze();
        Trees trees = Trees.instance(task);
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(Path.of(args[0])))) {
            for (CompilationUnitTree unit : units) {
                new Scanner(trees, unit, out).scan(unit, null);
            }
        }
    }

    private static final class Scanner extends TreePathScanner<Void, Void> {
        private final Trees trees;
        private final CompilationUnitTree unit;
        private final PrintWriter out;

        Scanner(Trees trees, CompilationUnitTree unit, PrintWriter out) {
            this.trees = trees;
            this.unit = unit;
            this.out = out;
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Void unused) {
            emit(node);
            return super.visitMethodInvocation(node, unused);
        }

        @Override
        public Void visitMemberReference(MemberReferenceTree node, Void unused) {
            emit(node);
            return super.visitMemberReference(node, unused);
        }

        private void emit(Tree node) {
            Element element = trees.getElement(getCurrentPath());
            if (!(element instanceof ExecutableElement method)
                    || !(method.getEnclosingElement() instanceof TypeElement owner)) {
                return;
            }
            TreePath declared = trees.getPath(method);
            String declaredFile = declared == null ? "" : declared.getCompilationUnit().getSourceFile().getName();
            long line = unit.getLineMap().getLineNumber(trees.getSourcePositions().getStartPosition(unit, node));
            out.println(unit.getSourceFile().getName() + "\t" + line + "\t" + owner.getQualifiedName() + "\t"
                    + method.getSimpleName() + "\t" + declaredFile + "\t" + enclosing());
        }

        private String enclosing() {
            String cls = "";
            String meth = "";
            for (TreePath p = getCurrentPath(); p != null; p = p.getParentPath()) {
                if (meth.isEmpty() && p.getLeaf() instanceof MethodTree m) {
                    meth = m.getName().toString();
                }
                if (p.getLeaf() instanceof ClassTree c && cls.isEmpty()) {
                    cls = c.getSimpleName().toString();
                }
            }
            return cls + "." + meth;
        }
    }
}

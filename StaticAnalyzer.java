import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.tree.JCTree;
import org.apache.commons.io.FileUtils;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

import static com.sun.tools.javac.api.JavacTool.create;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;
import static java.util.stream.StreamSupport.stream;

/**
 * Created by Baoyi Chen on 2017/4/27.
 */
public class StaticAnalyzer {
    public static void main(String[] args) throws IOException {
        String folder = "C:/workspace/redis-replicator/src/main/java";
        Collection<File> files = FileUtils.listFiles(new File(folder), new String[]{"java"}, true);
        JavacFileManager fm = create().getStandardFileManager(null, null, null);
        JavacTask task = create().getTask(null, fm, null, emptyList(), null, fm.getJavaFileObjects(files.toArray(new File[]{})));
        Iterable<? extends CompilationUnitTree> it = task.parse(); task.analyze();
        Predicate<Method> test = v -> v.owner.tsym.toString().startsWith("com.moilioncircle");
        Map<Method, Integer> map = stream(it.spliterator(), true).flatMap(e -> scan(e, test).stream()).collect(toConcurrentMap(s -> s, s -> 1, Integer::sum));
        map.entrySet().stream().sorted(Map.Entry.<Method,Integer>comparingByValue().reversed()).limit(20).forEach(System.out::println);
    }

    private static <T extends CompilationUnitTree> List<Method> scan(T t, Predicate<Method> predicate) {
        List<Method> rs = new ArrayList<>();
        new TreePathScanner<Void, List<Method>>() {

            public Void visitMethodInvocation(MethodInvocationTree var1, List<Method> var2) {
                JCTree.JCMethodInvocation mi = (JCTree.JCMethodInvocation) var1;
                Method m = null; String source = t.getSourceFile().getName();
                long line = t.getLineMap().getLineNumber(mi.getStartPosition());
                if (var1.getMethodSelect() instanceof JCTree.JCIdent) {
                    JCTree.JCIdent ident = (JCTree.JCIdent) var1.getMethodSelect();
                    m = new Method((Symbol.MethodSymbol) ident.sym, source, line);
                } else if (var1.getMethodSelect() instanceof JCTree.JCFieldAccess) {
                    JCTree.JCFieldAccess fieldAccess = (JCTree.JCFieldAccess) var1.getMethodSelect();
                    m = new Method((Symbol.MethodSymbol) fieldAccess.sym, source, line);
                }
                if (m == null || !predicate.test(m)) return super.visitMethodInvocation(var1, var2);
                var2.add(m); return super.visitMethodInvocation(var1, var2);
            }

        }.scan(t, rs);
        return rs;
    }

    private static class Method {
        private long line;
        private Type owner;
        private String name;
        private String source;
        private Type returnType;
        private List<Type> thrown;
        private List<Type> argTypes;
        private List<String> argNames;
        private Set<Modifier> modifiers;

        public Method(Symbol.MethodSymbol ms, String source, long line) {
            this.line = line;
            this.source = source;
            this.owner = ms.owner.type;
            this.thrown = ms.getThrownTypes();
            this.modifiers = ms.getModifiers();
            this.returnType = ms.getReturnType();
            this.name = ms.getSimpleName().toString();
            this.argTypes = ms.getParameters().stream().map(e -> e.type).collect(toList());
            this.argNames = ms.getParameters().stream().map(e -> e.toString()).collect(toList());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Method method = (Method) o;

            if (!owner.equals(method.owner)) return false;
            if (!returnType.equals(method.returnType)) return false;
            if (!name.equals(method.name)) return false;
            return argTypes.equals(method.argTypes);
        }

        @Override
        public int hashCode() {
            int result = owner.hashCode();
            result = 31 * result + returnType.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + argTypes.hashCode();
            return result;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[owner:").append(owner).append(" method:");
            builder.append(modifiers.stream().map(Modifier::toString).collect(joining(" ")));
            builder.append(" ").append(returnType.tsym.getSimpleName()).append(" ").append(name).append("(");
            assert argTypes.size() == argNames.size();
            for (int i = 0; i < argTypes.size(); i++) {
                builder.append(argTypes.get(i).tsym.getSimpleName()).append(" ");
                builder.append(argNames.get(i));
                if (i < argTypes.size() - 1) builder.append(",");
            }
            builder.append(")");
            if (!thrown.isEmpty()) {
                builder.append(" throws ");
                builder.append(thrown.stream().map(e -> e.tsym.getSimpleName()).collect(joining(",")));
            }
            builder.append("]");
            return builder.toString();
        }
    }
}

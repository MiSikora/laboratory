import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {
  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Usage: DepsDiff.java previous.txt current.txt");
      System.exit(1);
      return;
    }

    var previous = readAllDeps(args[0]);
    var current = readAllDeps(args[1]);

    var additions = new ArrayList<Dep>(current.size());
    var changes = new ArrayList<DepChange>(current.size());
    var removals = new ArrayList<Dep>(previous.size());

    for (var entry : current.entrySet()) {
      var currentKey = entry.getKey();
      var currentDep = entry.getValue();
      var previousDep = previous.get(currentKey);

      if (previousDep == null) {
        additions.add(currentDep);
      } else if (!currentDep.version().equals(previousDep.version())) {
        changes.add(new DepChange(previousDep, currentDep));
      }
    }
    for (var entry : previous.entrySet()) {
      if (!current.containsKey(entry.getKey())) {
        removals.add(entry.getValue());
      }
    }

    if (additions.isEmpty() && changes.isEmpty() && removals.isEmpty()) {
      return;
    }

    var builder = new StringBuilder();
    for (var dep : additions) {
      builder
          .append("- Added `")
          .append(dep.key().group())
          .append(':')
          .append(dep.key().name())
          .append(':')
          .append(dep.version())
          .append("`.\n");
    }
    for (var change : changes) {
      builder
          .append("- Changed `")
          .append(change.current().key().group())
          .append(':')
          .append(change.current.key().name())
          .append("` from `")
          .append(change.previous().version())
          .append("` to `")
          .append(change.current().version())
          .append("`.\n");
    }
    for (var dep : removals) {
      builder
          .append("- Removed `")
          .append(dep.key().group())
          .append(':')
          .append(dep.key().name())
          .append(':')
          .append(dep.version())
          .append("`.\n");
    }
    System.out.println(builder.toString().trim());
  }

  private static Map<DepKey, Dep> readAllDeps(String path) throws Exception {
    return Files.readAllLines(Path.of(path)).stream()
        .map(Main::toDependency)
        .collect(Collectors.toMap(Dep::key, Function.identity()));
  }

  private static Dep toDependency(String value) {
    var group = value.substring(0, value.indexOf(':'));
    var name = value.substring(value.indexOf(':') + 1, value.lastIndexOf(':'));
    var version = value.substring(value.lastIndexOf(':') + 1);
    return new Dep(new DepKey(group, name), version);
  }

  record DepKey(String group, String name) {}

  record Dep(DepKey key, String version) {}

  record DepChange(Dep previous, Dep current) {}
}

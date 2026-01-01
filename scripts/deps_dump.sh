#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <version>" >&2
  exit 1
fi

version="$1"

dump_deps() {
  local config_type="$1"
  local config
  if [[ "$config_type" == "android" ]]; then
    config="releaseRuntimeClasspath"
  else
    config="runtimeClasspath"
  fi
  local module_path="$2"
  echo "Dumping dependencies for ${module_path}."

  local module_dir="./${module_path#:}"
  module_dir="${module_dir//://}"
  local output_file="${module_dir}/api/deps/${version}.txt"

  mkdir -p "$(dirname "$output_file")"
  ./gradlew "${module_path}:dependencies" --configuration "$config" --console plain |
    sed -En 's/^[+\\]---[[:space:]](.*)/\1/p' |
    sed -E '/^project[[:space:]]/d' |
    sed -E 's/[[:space:]]+\(\*\)$//' |
    sort \
      >"$output_file"
}

dump_deps jvm :laboratory:runtime
dump_deps jvm :laboratory:generator
dump_deps jvm :laboratory:gradle-plugin
dump_deps android :laboratory:data-store
dump_deps android :laboratory:shared-preferences
dump_deps android :laboratory:inspector
dump_deps android :laboratory:hyperion-plugin

echo "Dependencies dump done."

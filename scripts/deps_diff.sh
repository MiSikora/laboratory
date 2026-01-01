#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 2 ]]; then
  echo "Usage: $0 <previous_version> <current_version>" >&2
  exit 1
fi

previous_version="$1"
current_version="$2"

diff_deps() {
  local module_path="$1"
  local module_dir="./${module_path#:}"
  module_dir="${module_dir//://}"
  local previous_deps="${module_dir}/api/deps/${previous_version}.txt"
  local current_deps="${module_dir}/api/deps/${current_version}.txt"

  if [[ ! -f "$previous_deps" ]]; then
    echo "Missing previous deps file: $previous_deps" >&2
    exit 1
  fi
  if [[ ! -f "$current_deps" ]]; then
    echo "Missing current deps file: $current_deps" >&2
    exit 1
  fi

  java ./scripts/DepsDiff.java "$previous_deps" "$current_deps"
}

runtime=$(diff_deps :laboratory:runtime)
generator=$(diff_deps :laboratory:generator)
gradle_plugin=$(diff_deps :laboratory:gradle-plugin)
data_store=$(diff_deps :laboratory:data-store)
shared_preferences=$(diff_deps :laboratory:shared-preferences)
inspector=$(diff_deps :laboratory:inspector)
hyperion_plugin=$(diff_deps :laboratory:hyperion-plugin)

if [[ -n "$runtime" ||
  -n "$generator" ||
  -n "$gradle_plugin" ||
  -n "$data_store" ||
  -n "$shared_preferences" ||
  -n "$inspector" ||
  -n "$hyperion_plugin" ]]; then
  echo "### Dependencies"

  if [[ -n "$runtime" ]]; then
    echo ""
    echo "#### \`io.mehow.laboratory:laboratory\`"
    echo "$runtime"
  fi

  if [[ -n "$generator" ]]; then
    echo ""
    echo "#### \`io.mehow.laboratory:laboratory-generator\`"
    echo "$generator"
  fi

  if [[ -n "$gradle_plugin" ]]; then
    echo ""
    echo "#### \`io.mehow.laboratory:laboratory-gradle-plugin\`"
    echo "$gradle_plugin"
  fi

  if [[ -n "$data_store" ]]; then
    echo ""
    echo "#### \`io.mehow.laboratory:laboratory-data-store'\`"
    echo "$data_store"
  fi

  if [[ -n "$shared_preferences" ]]; then
    echo ""
    echo "#### \`io.mehow.laboratory:laboratory-shared-preferences\`"
    echo "$shared_preferences"
  fi

  if [[ -n "$inspector" ]]; then
    echo ""
    echo "#### \`io.mehow.laboratory:laboratory-inspector\`"
    echo "$inspector"
  fi

  if [[ -n "$hyperion_plugin" ]]; then
    echo ""
    echo "#### \`io.mehow.laboratory:laboratory-hyperion-plugin\`"
    echo "$hyperion_plugin"
  fi
fi

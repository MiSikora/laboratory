fun isCi(): Boolean {
  return System.getenv("CI")?.toBoolean() == true
}

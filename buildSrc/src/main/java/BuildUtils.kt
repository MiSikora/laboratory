fun isCi(): Boolean {
  return System.getenv("IS_CI")?.toBoolean() == true
}

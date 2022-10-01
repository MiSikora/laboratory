import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.sample.ci.SampleFlag

suspend fun main() {
  val laboratory = Laboratory.inMemory()
  println(laboratory.experimentIs(SampleFlag.OptionA))
}

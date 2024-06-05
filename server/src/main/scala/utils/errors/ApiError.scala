package utils.errors

import cats.data.{ NonEmptyList, NonEmptyMap }
import cats.syntax.option._
import sttp.tapir.EndpointIO.Example
import sttp.tapir.json.tethysjson.jsonBody
import sttp.tapir.{ oneOf, oneOfVariant, statusCode, EndpointOutput, Schema }
import tethys.{ JsonReader, JsonWriter }
import utils.errors.Domain.{ ErrorCode, ErrorMsg }

final case class ApiError(
    errorCode: ErrorCode,
    errorMsg: ErrorMsg
)

object ApiError {
  trait Descriptor[-A] {
    def message(value: A): ErrorMsg
    def code(value: A): ErrorCode
    def level(value: A): ErrorLevel
  }

  object Descriptor {
    def apply[A](implicit descriptor: Descriptor[A]): Descriptor[A] = descriptor
  }

  implicit def jsonReader: JsonReader[ApiError] = JsonReader.builder
    .addField[ErrorCode]("errorCode")
    .addField[ErrorMsg]("errorMsg")
    .buildReader(ApiError(_, _))

  implicit def jsonWriter: JsonWriter[ApiError] =
    JsonWriter
      .obj[ApiError]
      .addField("errorCode")(_.errorCode)
      .addField("errorMsg")(_.errorMsg)

  implicit def schema: Schema[ApiError] =
    Schema
      .derived[ApiError]
      .modify(_.errorCode)(_.description("Внутренний код ошибки"))
      .modify(_.errorMsg)(_.description("Текст ошибки"))

  private def genApiErrors[E](variants: NonEmptyList[E])(implicit
      descriptor: Descriptor[E]
  ): NonEmptyMap[ErrorLevel, NonEmptyList[ApiError]] = variants
    .map { v =>
      (
        descriptor.level(v),
        ApiError(
          descriptor.code(v),
          descriptor.message(v)
        )
      )
    }
    .groupByNem { case (level, _) => level }
    .map(_.map { case (_, err) => err })

  def makeOneOf[E](variants: NonEmptyList[E])(implicit
      descriptor: Descriptor[E]
  ): EndpointOutput.OneOf[ApiError, ApiError] = {
    val errorVariants = genApiErrors[E](variants).toNel
      .map { case (l, errs) =>
        val apiErrorExamples =
          errs.map { err =>
            Example.of(
              err,
              err.errorCode.value.some
            )
          }.toList

        oneOfVariant(
          statusCode(l.code).and(
            jsonBody[ApiError].examples(apiErrorExamples)
          )
        )
      }

    oneOf(
      errorVariants.head,
      errorVariants.tail: _*
    )
  }
}

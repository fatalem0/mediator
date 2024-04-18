package integrations.discogs

import cats.Applicative
import integrations.discogs.Domain.Errors.DiscogsTokenClientError
import integrations.discogs.Domain.{OauthToken, TokenSecret, TokenValue}
import integrations.discogs.configs.DiscogsConfig
import sttp.client3._
import tethys.JsonReader
import tethys.readers.tokens.TokenIteratorProducer
import tofu.Handle
import tofu.syntax.feither._
import tofu.syntax.handle._

trait DiscogsTokenClient[F[_]] {
  def getRequestToken: F[Either[DiscogsTokenClientError, OauthToken]]

  def getAccessToken(
      tokenValue: TokenValue,
      tokenSecret: TokenSecret
  ): F[Either[DiscogsTokenClientError, OauthToken]]
}

object DiscogsTokenClient {
  final private class Impl[F[_]: Applicative: Handle[*[_], SttpClientException]](
      backend: SttpBackend[F, Any],
      config: DiscogsConfig
  )(implicit tokenIteratorProducer: TokenIteratorProducer) extends DiscogsTokenClient[F] {
    override def getRequestToken: F[Either[DiscogsTokenClientError, OauthToken]] =
      sendRequest[OauthToken](
        basicRequest
          .get(uri"${config.uri}/request_token")
          .headers(
            Map(
              "User-Agent" -> "Mediator/1.0",
              "Authorization" ->
                s"""
                  |OAuth
                  |  oauth_consumer_key="${config.consumerKey}",
                  |  oauth_signature_method="PLAINTEXT",
                  |  oauth_timestamp="current_timestamp",
                  |  oauth_nonce="random_string_or_timestamp",
                  |  oauth_version="1.0",
                  |  oauth_signature="${config.consumerSecret}%26"
                  |""".stripMargin
            )
          )
          .response(DiscogsTokenClientError.responseAs[OauthToken])
      )

    override def getAccessToken(oauthToken: Token): F[Either[DiscogsTokenClientError, OauthToken]] =
      sendRequest[OauthToken](
        basicRequest
          .post(uri"${config.uri}/access_token")
          .headers(
            Map(
              "User-Agent" -> "Mediator/1.0",
              "Authorization" ->
                s"""
                   |OAuth
                   |  oauth_consumer_key="${config.consumerKey}",
                   |  oauth_nonce="random_string_or_timestamp",
                   |  oauth_token=$oauthToken,
                   |  oauth_signature=${config.consumerSecret},
                   |  oauth_signature_method="PLAINTEXT",
                   |  oauth_timestamp="current_timestamp",
                   |  oauth_verifier="users_verifier"
                   |""".stripMargin
            )
          )
          .response(DiscogsTokenClientError.responseAs[OauthToken])
      )

    private def sendRequest[T: JsonReader](
        req: Request[String, Any]
    ): F[Either[DiscogsTokenClientError, T]] =
      backend
        .send[String, Any](req)
        .attempt[SttpClientException]
        .leftMapIn[DiscogsTokenClientError](DiscogsTokenClientError.Connect)
        .mapIn(_.body)
  }

  def make[F[_]: Applicative: Handle[*[_], SttpClientException]](
      backend: SttpBackend[F, Any],
      config: DiscogsConfig
  )(implicit tokenIteratorProducer: TokenIteratorProducer): DiscogsTokenClient[F] =
    new Impl(backend, config)
}

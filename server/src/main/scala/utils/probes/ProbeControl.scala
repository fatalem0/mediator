package utils.probes

import cats.Functor
import cats.effect.Ref
import cats.effect.Sync
import cats.syntax.functor._

class ProbeControl[F[_]] private (ref: Ref[F, ProbeStatus]) {
  def status: F[ProbeStatus] = ref.get
  def ready: F[Unit]         = ref.set(ProbeStatus.Ready)
  def notReady: F[Unit]      = ref.set(ProbeStatus.NotReady)
}

object ProbeControl {
  def make[I[_]: Sync: Functor]: I[ProbeControl[I]] = Ref[I].of[ProbeStatus](
    ProbeStatus.NotReady
  ).map(new ProbeControl(_))
}

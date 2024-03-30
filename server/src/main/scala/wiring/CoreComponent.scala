package wiring

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import configs.AppConf
import tofu.logging.Logging
import tofu.time.Sleep
import utils.hooks.ShutdownHook
import utils.probes.ProbeControl

class CoreComponent[I[_]](implicit
    val conf: AppConf,
    val logMakerI: Logging.Make[I],
    val probeControl: ProbeControl[I],
    val shutdownHook: ShutdownHook[I]
)

object CoreComponent {
  def make[I[_]: Sync: Sleep]: I[CoreComponent[I]] =
    for {
      implicit0(conf: AppConf) <- AppConf.load[I]
      implicit0(logMakeI: Logging.Make[I]) = Logging.Make.plain[I]
      implicit0(probeControl: ProbeControl[I]) <- ProbeControl.make[I]
      implicit0(shutdownHook: ShutdownHook[I]) =
        ShutdownHook.make[I](conf.shutdown.gracePeriod, probeControl)

      core = new CoreComponent[I]
    } yield core
}

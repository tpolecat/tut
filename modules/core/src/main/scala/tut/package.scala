package object tut {
  import Zed.{IO, StateT}

  type Tut[A] = StateT[IO, TutState, A]
}

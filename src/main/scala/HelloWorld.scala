package intro

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum
import chisel3.stage.ChiselStage._

class HelloWorld extends Module {
  val io = IO(new Bundle {
    val cond = Input(Bool())
    val out = Output(UInt(8.W))
  })

  when(io.cond) {
    io.out := 1.U
  } .otherwise {
    io.out := 2.U
  }
}

object S extends ChiselEnum {
  val A, B, C = Value
}

class StateMachine extends Module {
  val io = IO(new Bundle {
    val in = Input(Bool())
    val stateIn = Input(S())
    val loadState = Input(Bool())
    val stateOut = Output(S())
  })

  val state = RegInit(S.A)

  when(io.loadState) {
    state := io.stateIn
  } .otherwise {
    switch (state) {
      is (S.A) {
        state := Mux(io.in, S.A, S.B)
      }
      is (S.B) {
        when (io.in) {
          state := S.B
        } .otherwise {
          state := S.C
        }
      }
    }
  }

  io.stateOut := state
}

object State extends ChiselEnum {
  val sIdle, sLoad, sProcess, sDone = Value
}

class SimpleStateMachine extends Module {
  val io = IO(new Bundle {
    val start = Input(Bool())
    val dataIn = Input(UInt(8.W))
    val dataOut = Output(UInt(8.W))
    val done = Output(Bool())
  })

  // State register
  val state = RegInit(State.sIdle)

  // Memory
  val mem = Mem(16, UInt(8.W))

  // Wires with initial values
  val memAddr = RegInit(0.U(4.W))
  val memData = RegInit(0.U(8.W))

  io.done := false.B  // Default value for io.done

  // Logic
  switch(state) {
    is(State.sIdle) {
      when(io.start) {
        state := State.sLoad
      }
    }
    is(State.sLoad) {
      mem(memAddr) := io.dataIn
      when(memAddr === 15.U) {
        state := State.sProcess
      } .otherwise {
        memAddr := memAddr + 1.U
      }
    }
    is(State.sProcess) {
      memData := mem(memAddr)
      when(memAddr === 0.U) {
        state := State.sDone
      } .otherwise {
        memAddr := memAddr - 1.U
      }
    }
    is(State.sDone) {
      io.done := true.B
    }
  }

  io.dataOut := memData
}

class SimpleQueue(width: Int, depth: Int) extends Module {
  val io = IO(new Bundle {
    val enq = Flipped(Decoupled(UInt(width.W)))
    val deq = Decoupled(UInt(width.W))
  })

  val queue = Queue(io.enq, depth)

  io.deq <> queue

  // For demonstration purposes, you can add some logic here if needed.
}


object VerilogMain extends App {
  println("-----------------------------------")
  println(emitFirrtl(new HelloWorld)) // 现在我们要关注前面这部分了
  println("-----------------------------------")
  println(emitFirrtl(new StateMachine))
  println("-----------------------------------")
  println(emitFirrtl(new SimpleStateMachine))
  println("-----------------------------------")
  println(emitFirrtl(new SimpleQueue(8, 4)))
  println("-----------------------------------")
  // println(emitSystemVerilog(new HelloWorld))
}

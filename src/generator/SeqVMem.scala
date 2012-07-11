package torture

import scala.collection.mutable.ArrayBuffer
import Rand._

class SeqVMem(xregs: HWRegPool, mem: VMem) extends InstSeq
{
  def helper_setup_address(reg_addr: Reg, reg_helper: Reg, baseaddr: Int) =
  {
    insts += UTIDX(reg_helper)
    insts += ADDI(reg_addr, reg_read_zero(xregs), Imm(mem.ut_size))
    insts += MUL(reg_helper, reg_helper, reg_addr)
    insts += LA(reg_addr, BaseImm(mem.toString, baseaddr))
    insts += ADD(reg_addr, reg_addr, reg_helper)
  }
  def seq_load_addrfn(op: Opcode, addrfn: (Int) => Int) = () =>
  {
    val reg_addr   = reg_write_hidden(xregs)
    val reg_dest   = reg_write_visible(xregs)
    val addr = addrfn(mem.ut_size)
    val imm = rand_imm()

    helper_setup_address(reg_addr, reg_dest, addr-imm)
    insts += op(reg_dest, RegImm(reg_addr, imm))
  }

  def seq_store_addrfn(op: Opcode, addrfn: (Int) => Int) = () =>
  {
    val reg_addr   = reg_write_hidden(xregs)
    val reg_helper = reg_write_visible(xregs)
    val reg_src    = reg_read_visible(xregs)
    val addr = addrfn(mem.ut_size)
    val imm = rand_imm()

    helper_setup_address(reg_addr, reg_helper, addr-imm)
    insts += op(reg_src, RegImm(reg_addr, imm))
  }

  def seq_amo_addrfn(op: Opcode, addrfn: (Int) => Int) = () =>
  {
    val reg_addr = reg_write_hidden(xregs)
    val reg_dest = reg_write_visible(xregs)
    val reg_src = reg_read_visible(xregs)
    val addr = addrfn(mem.ut_size)

    helper_setup_address(reg_addr, reg_dest, addr)
    insts += op(reg_dest, reg_src, RegImm(reg_addr, 0))
  }

  val candidates = new ArrayBuffer[() => insts.type]

  candidates += seq_load_addrfn(LB, rand_addr_b)
  candidates += seq_load_addrfn(LBU, rand_addr_b)
  candidates += seq_load_addrfn(LH, rand_addr_h)
  candidates += seq_load_addrfn(LHU, rand_addr_h)
  candidates += seq_load_addrfn(LW, rand_addr_w)
  candidates += seq_load_addrfn(LWU, rand_addr_w)
  candidates += seq_load_addrfn(LD, rand_addr_d)

  candidates += seq_store_addrfn(SB, rand_addr_b)
  candidates += seq_store_addrfn(SH, rand_addr_h)
  candidates += seq_store_addrfn(SW, rand_addr_w)
  candidates += seq_store_addrfn(SD, rand_addr_d)

  candidates += seq_amo_addrfn(AMOADD_W, rand_addr_w)
  candidates += seq_amo_addrfn(AMOSWAP_W, rand_addr_w)
  candidates += seq_amo_addrfn(AMOAND_W, rand_addr_w)
  candidates += seq_amo_addrfn(AMOOR_W, rand_addr_w)
  candidates += seq_amo_addrfn(AMOMIN_W, rand_addr_w)
  candidates += seq_amo_addrfn(AMOMINU_W, rand_addr_w)
  candidates += seq_amo_addrfn(AMOMAX_W, rand_addr_w)
  candidates += seq_amo_addrfn(AMOMAXU_W, rand_addr_w)

  candidates += seq_amo_addrfn(AMOADD_D, rand_addr_d)
  candidates += seq_amo_addrfn(AMOSWAP_D, rand_addr_d)
  candidates += seq_amo_addrfn(AMOAND_D, rand_addr_d)
  candidates += seq_amo_addrfn(AMOOR_D, rand_addr_d)
  candidates += seq_amo_addrfn(AMOMIN_D, rand_addr_d)
  candidates += seq_amo_addrfn(AMOMINU_D, rand_addr_d)
  candidates += seq_amo_addrfn(AMOMAX_D, rand_addr_d)
  candidates += seq_amo_addrfn(AMOMAXU_D, rand_addr_d)

  rand_pick(candidates)()
}
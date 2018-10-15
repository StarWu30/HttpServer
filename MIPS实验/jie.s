 .data	
CONTROL: .word32 0x10000
DATA:         .word32   0x10008

.text
main:
lwu $t8,DATA($zero)	; $t8 = address of DATA register
lwu $t9,CONTROL($zero)	; $t9 = address of CONTROL register

daddi $v0,$zero,8  ;输入
sd $v0,0($t9)
ld $t1,0($t8)


daddi $t2,$zero,1
daddi $t3,$zero,2

cal:
slt $t0,$t1,$t3
bne $t0,$0,out #s1<2 goto out
dmul $t2,$t2,$t1
daddi $t1,$t1,-1
j    cal

out:
daddi $v0,$zero,2      ; set for ascii output
sd $t2,0($t8)           ; write address of message to DATA register
sd $v0,0($t9)           ; make it happen

halt

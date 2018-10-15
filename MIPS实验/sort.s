.data
array: .word 8,6,3,7,1,0,9,4,5,2
str1:	 .asciiz	"before sort the array is:\n"
str2:  .asciiz	"after sort the array is:\n"
start: .word32	0x100

CONTROL: .word32 0x10000
DATA:		 .word32 0x10008


.text
main:
lwu $t8, DATA($zero)
lwu $t9, CONTROL($zero)
ld $sp, start($zero)  

#输出字符串1
daddi $t1, $zero, str1
daddi $v0, $zero, 4
sd $t1, 0($t8)
sd $v0, 0($t9)

#输出原数组
daddi $a0, $zero, array
daddi $a1, $zero, 10			
jal printArr

#输出字符串2
daddi $t1, $zero, str2
daddi $v0, $zero, 4
sd $t1, 0($t8)
sd $v0, 0($t9)

#排序
daddi $a0, $zero, array
daddi $a1, $zero, 10
jal sort

#输出新数组
daddi $a0, $zero, array
daddi $a1, $zero, 10
jal printArr

halt

sort:
daddi $sp, $sp,-48
#dadd	$s7, $ra, $zero

sd 		$ra, 40($sp)
sd		$s4, 32($sp)
sd 		$s3, 24($sp)
sd 		$s2, 16($sp)
sd		$s1, 8($sp)
sd		$s0, 0($sp)

dadd 	$s2, $a0, $zero		# s2 = *a
dadd	$s3, $a1, $zero		# s3 = n

daddi $s0, $zero, 1		  # i = 1
for1tst:
slt		$t0, $s0, $s3			# i < n ?
beq		$t0, $zero, exit1	# 为0跳出循环

daddi	$s1, $zero, 0			# s1 = j = 0
dsub  $s4, $s3, $s0			# s4 = n - i
for2tst:
slt 	$t0, $s1, $s4			# j < n - i ?				
beq 	$t0, $zero, exit2
dsll	$t1, $s1, 3				# t1 = 8 * j
dadd 	$t2, $s2, $t1			#	t2 = *a + 8 * j
lw		$t3, 0($t2)				# t3 = a[j]
lw		$t4, 8($t2)				# t4 = a[j+1]
slt		$t0, $t4, $t3			#	a[j+1] < a[j]? 
beq		$t0, $zero, continue2	# 为1就交换

dadd	$a0, $s2, $zero		# a0 = s2 = *a
dadd	$a1, $s1, $zero		# a1 = j
jal		swap							# 交换
continue2:
daddi $s1, $s1, 1				# j++
j			for2tst

exit2:
daddi	$s0, $s0, 1				# i++
j			for1tst

exit1:
ld		$s0, 0($sp)
ld		$s1, 8($sp)
ld		$s2, 16($sp)
ld		$s3, 24($sp)
ld		$s4, 32($sp)
ld		$ra, 40($sp)
daddi $sp, $sp, 48
#dadd	$ra, $s7, $zero
jr		$ra

swap:
dsll	$t0, $a1, 3			# t0 = 8 * j
dadd	$t0, $a0, $t1		# t0 = *a + 8*j
lw		$t1, 0($t0)			# t1 = a[j]
lw		$t2, 8($t0)			# t2 = a[j+1]
sw		$t2, 0($t0)			# a[j] = t2
sw		$t1, 8($t0)			#	a[j+1] = t1
jr		$ra

printArr:							#输出数组
daddi $sp, $sp, -8
sw		$s1, 0($sp)
dadd  $t2, $zero, $a1	; t2 = a1	
dadd  $s1, $zero, $a0	; s1 = a0 = *a 
pfor1:
ld 		$t1, 0($s1)
daddi $v0, $zero, 1
sd		$t1, 0($t8)
sd 		$v0, 0($t9)
daddi $s1, $s1,8
daddi $t2,$t2,-1
bnez 	$t2,pfor1
lw		$s1, 0($sp)
daddi	$sp, $sp, 8
jr		$ra
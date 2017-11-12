cpu:  
cat /proc/cpuinfo | grep "processor" | wc -l  
  
内存:  
free -m   
已用内存：used-buffers-cached = -buffers/cache  
可用内存: free+buffers+cached = +buffers/cache  
  
硬盘:  
fdisk -l  
df -h  
  
IO性能:  
dnf install sysstat  
iostat -d -x -k 1 10  
% util 接近100%， IO满负荷， 磁盘瓶颈  
await 应该低于5ms，大于5ms表示磁盘IO压力大, 更换磁盘  
  
目录大小:  
du -sh /data  
  
负载:  
uptime  
w  
top  
注意load average输出值, 3个值不能大于逻辑cpu个数, 4个cpu，如果三个值长期大于4，说明cpu繁忙  
vmstat 1 4  
user% + sys% < 70% 表示系统性能好, >85% 系统性能比较糟糕  
user% 表示用户模式cpu百分比， sys%系统模式cpu百分比  
  
内核:  
uname -a  
uname -r  
ls -lF /| grep /$ | grep lib64(32位还是64位)  
  
host名:  
编辑/etc/sysconfig/network  
/etc/hosts  
dns域名解析/etc/resolv.conf  
  
网络:  
netstat -an | grep -v unix  
traceroute www.163.com  
激活网卡,配置网关/etc/sysconfig/network-scripts/ifcfg-eth0（ONBOOT=yes）  
  
用户:  
finger  
  
端口:  
lsof -i:8080  
  
进程:  
ps -aux | grep -v grep | grep redis  
pgrep redis  
killall redis  
  
日志:  
/var/log/messages  
/var/log/secure(安全)  
/var/log/wtmp(登陆者信息 last命令查看)  
/var/log/lastlog  
  
最大文件打开数:  
ulimit -a  
  
vim常用:  
hjkl方向键  
ctrl+f向前翻页  
ctrl+b向后翻页  
0 home  
$ end  
n <enter> 向下移动n行  
/word 向下查找word  
? word 向上查找word  
:n1, n2s/word1/word2/g 在n1与n2之间查找word1并替换为word2  
:1, $s/word1/word2/g 第一行与最后一行查找work1并替换word2  
:1, $s/word1/word2/gc 第一行与最后一行查找work1并替换word2，并让用户确认  
Dd 删除光标在的那一行  
<n>dd 删除光标所在列的向下<n>行  
Yy 复制光标所在行  
<n>yy 复制光标所在列的<n>列  
p or P 粘贴在光标下一列或上一列  
U 恢复前一个动作  











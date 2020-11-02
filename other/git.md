### 合并多个commit  
git rebase –i HEAD~2 （合并前两个提交）  
或者  
git log  
git rebase -i `<SHA>` (不包含)  
进入编辑界面,将pick修改为squash 保存  
进入commit消息界面，合并commit消息（#开头的是注释）  
wq保存退出  
提交到远端  
git push --force origin master  

合并前两个提交并更改sign-off信息
git rebase -i HEAD~2 -x "git commit --amend --author 'Baoyi Chen <chen.bao.yi@qq.com>' --no-edit"

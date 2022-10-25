<p align="center">
<a href="https://openjdk.java.net/"><img src="https://img.shields.io/badge/Java-8+-green?logo=java&logoColor=white" alt="Java support"></a>
<a href="https://www.apache.org/licenses/LICENSE-2.0.html"><img src="https://img.shields.io/github/license/dk900912/file-watcher?color=4D7A97&logo=apache" alt="License"></a>
<a href="https://search.maven.org/search?q=a:file-watcher"><img src="https://img.shields.io/maven-central/v/io.github.dk900912/file-watcher?logo=apache-maven" alt="Maven Central"></a>
<a href="https://github.com/dk900912/file-watcher/releases"><img src="https://img.shields.io/github/release/dk900912/file-watcher.svg" alt="GitHub release"></a>
<a href="https://github.com/dk900912/file-watcher/stargazers"><img src="https://img.shields.io/github/stars/dk900912/file-watcher" alt="GitHub Stars"></a>
<a href="https://github.com/dk900912/file-watcher/fork"><img src="https://img.shields.io/github/forks/dk900912/file-watcher" alt="GitHub Forks"></a>
<a href="https://github.com/dk900912/file-watcher/issues"><img src="https://img.shields.io/github/issues/dk900912/file-watcher" alt="GitHub issues"></a>
<a href="https://github.com/dk900912/efile-watcher/graphs/contributors"><img src="https://img.shields.io/github/contributors/dk900912/file-watcher" alt="GitHub Contributors"></a>
<a href="https://github.com/dk900912/file-watcher"><img src="https://img.shields.io/github/repo-size/dk900912/file-watcher" alt="GitHub repo size"></a>
</p>

>> 很简单的功能，但由于之前写的很 low，这次实现尽量更加地面向对象罢了。

### How to use
```java
public class FileWatcherApplication {
    public static void main(String[] args) {
        FileSystemWatcher fileWatcher = new FileSystemWatcher();
        fileWatcher.addListener(new SimpleFileChangeListener());
        File directory1 = new File("C:\\Users\\dk900912\\IdeaProjects\\test");
        File directory2 = new File("C:\\Users\\dk900912\\IdeaProjects\\tset");
        fileWatcher.addSourceDirectories(Arrays.asList(directory1, directory2));
        fileWatcher.start();

        for (;;){

        }
    }
}
```
在上述两个目录中新增或者更新文件，然后将输出：
```
2022-10-25 21:36:15.122  INFO 21428 --- [   File Watcher] i.g.d.f.l.SimpleFileChangeListener       : 0=={======> C:\Users\dk900912\IdeaProjects\test\1.txt (MODIFY) <======}==0
2022-10-25 21:36:34.729  INFO 21428 --- [   File Watcher] i.g.d.f.l.SimpleFileChangeListener       : 0=={======> C:\Users\dk900912\IdeaProjects\tset\新建 Microsoft Excel 工作表.xlsx (ADD) <======}==0
```
> 建议自行实现`FileChangeListener`回调接口。

### You are welcome to enjoy it
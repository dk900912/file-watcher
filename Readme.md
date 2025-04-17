<p align="center">
<a href="https://openjdk.java.net/"><img src="https://img.shields.io/badge/Java-21+-green?logo=java&logoColor=white" alt="Java support"></a>
<a href="https://www.apache.org/licenses/LICENSE-2.0.html"><img src="https://img.shields.io/github/license/dk900912/file-watcher?color=4D7A97&logo=apache" alt="License"></a>
<a href="https://search.maven.org/search?q=a:file-watcher"><img src="https://img.shields.io/maven-central/v/io.github.dk900912/file-watcher?logo=apache-maven" alt="Maven Central"></a>
<a href="https://github.com/dk900912/file-watcher/releases"><img src="https://img.shields.io/github/release/dk900912/file-watcher.svg" alt="GitHub release"></a>
<a href="https://github.com/dk900912/file-watcher/stargazers"><img src="https://img.shields.io/github/stars/dk900912/file-watcher" alt="GitHub Stars"></a>
<a href="https://github.com/dk900912/file-watcher/fork"><img src="https://img.shields.io/github/forks/dk900912/file-watcher" alt="GitHub Forks"></a>
<a href="https://github.com/dk900912/file-watcher/issues"><img src="https://img.shields.io/github/issues/dk900912/file-watcher" alt="GitHub issues"></a>
<a href="https://github.com/dk900912/file-watcher/graphs/contributors"><img src="https://img.shields.io/github/contributors/dk900912/file-watcher" alt="GitHub Contributors"></a>
<a href="https://github.com/dk900912/file-watcher"><img src="https://img.shields.io/github/repo-size/dk900912/file-watcher" alt="GitHub repo size"></a>
</p>

# 1. 介绍

基于单个守护线程实现对本地多目标目录下的文件变更事件的监听功能。对于文件的重命名会先触发`ADD`事件，然后才是`DELETE`事件！`2.0.0`版本具备了文件快照本地持久化能力，这样在监听应用退出后，在这期间的文件变更事件依然可以被监听到！

# 2. 如何获取本组件

```xml
<dependency>
    <groupId>io.github.dk900912</groupId>
    <artifactId>file-watcher</artifactId>
    <version>2.0.4</version>
</dependency>
```
# 3. 快速入门

```java
public class FileWatcherApplication {
   public static void main(String[] args) {
      FileWatcherProperties fileWatcherProperties = new FileWatcherProperties();
      fileWatcherProperties.setDirectories(Arrays.asList("目录1", "目录2"));
      FileSystemWatcher fileWatcher = new FileSystemWatcher(fileWatcherProperties);
      fileWatcher.addListener(new SimpleFileChangeListener());
      fileWatcher.start();
   }
}
```
在上述两个目录中新增或者更新文件，然后将输出：
```
0=={======> 目录1\1.txt (MODIFY) <======}==0
0=={======> 目录2\新建 Microsoft Excel 工作表.xlsx (ADD) <======}==0
```
> 建议自行实现`FileChangeListener`回调接口。

# 4. 主要配置项

主要配置项均由`FileWatcherProperties`承载，默认配置如下：

| 配置项                      | 默认值                   | 说明                                                   |
|--------------------------|-----------------------|------------------------------------------------------|
| directories              | null                  | 监听目录列表，必须手动指定                                        |
| snapshotEnabled          | false                 | 文件快照功能                                               |
| acceptedStrategy         | null                  | 文件匹配策略，如果未显示指定策略即意味着采用`AnyFilter`，即只要匹配到任何文件变更就触发监听器 |
| pollInterval             | 1000ms                | 完整扫描周期的时间间隔，控制整体扫描频率                                 |
| quietPeriod              | 400ms                 | 文件变动后的静默观察期，用于确认变更是否稳定完成                             |
| daemon                   | true                  | 监听线程是否为守护线程                                          |
| name                     | "File Watcher"        | 监听线程名称                                               |
| remainingScans           | new AtomicInteger(-1) | 监听线程扫描文件目录的剩余次数，默认持续扫描                               |

# 5. 进阶

## 5.1 关于文件监听范围

默认监听目标目录下所有文件的变更事件。如果想要指定仅监听部分文件，通过`FileWatcherProperties`中的`acceptedStrategy`属性来指定目三种策略：
1. `ANY`：匹配任何文件，详见`AnyFilter`;
2. `SUFFIX`：匹配文件后缀，可指定多种后缀，详见`SuffixFilter`;
3. `REGEX`：匹配文件名正则表达式，但只能指定一个正则表达式Pattern，详见`RegexFilter`。

如果上述策略不满足需求，那么可以自行实现`FileFilter`接口并实现`accept()`方法，最后通过`FileSystemWatcher`的`replaceFileFilter()`方法来替换默认生成的`FileFilter`，这是最大的自由度。

```java
 /**
  * Typically, there is no need to replace the file filter, as a default {@link FileFilter}
  * is automatically provided based on the configuration in {@link FileWatcherProperties}.
  *
  * @param fileFilter the new {@link FileFilter} instance to set
  */
 public void replaceFileFilter(FileFilter fileFilter) {
     synchronized (this.monitor) {
         this.fileFilter = fileFilter;
     }
 }
```


## 5.2 为什么设计文件快照

> 文件快照本质上是一种基于`ObjectOutputStream`序列化与反序列化的本地持久化机制。

1. 如果监听应用退出后，在这期间（监听应用退出与监听应用重启之间）的文件变更事件是无法被监听到的；为了保持监听持续性，那么可以开启文件快照功能，默认关闭。
2. 如果监听目录文件数量很大，无论是啥原因导致的重启监听应用，那么都会重新扫描整个目录，势必要消耗一定时间；为了减少扫描时间，可以开启文件快照功能，默认关闭。

## 5.3 关于监听线程

> 在`FileSystemWatcher`中，为什么scan()方法只要探测到文件有变动，不立即退出`do-while`循环，而是直到没有文件变更才退出循环呢

### 5.3.1 分析

1. **防抖动机制**：
    - 通过多次检测确认文件变更已稳定
    - 避免处理不完整的中间状态（如：大文件写入/编辑器自动保存产生的临时文件）

2. **事件合并优化**：
    - 将高频连续变更合并为单个变更事件

3. **总结**

| 参数              | 防抖机制                            | 资源优化                      |
|-------------------|-----------------------------------|-----------------------------|
| `quietPeriod`     | 过滤短时间内的连续变更                | 减少事件触发次数              |
| `pollInterval`    | 控制最小事件处理间隔                  | 防止高频轮询消耗CPU           |

### 5.3.2 典型应用场景

```java
// 结构示意
sleep(pollInterval - quietPeriod); // 主等待期
do {
   takeSnapshot();
   sleep(quietPeriod);            // 静默观察期
} while (hasChange());
```

## 5.4 如何与Spring Boot整合

### 5.4.1 FileWatcherProperties属性自动装配问题

`FileWatcherProperties`由`file-watcher`组件定义且该组件并不依赖任何`Spring`组件，因此你无法在`FileWatcherProperties`头上追加`@ConfigurationProperties(prefix = "file-watcher")`注解。那么上层`Spring Boot`应用配置文件中所有以`file-watcher.`开头的相关配置项如何绑定到`FileWatcherProperties`实例中去呢？直接使用`Spring Boot`原生的`Relaxed Bingding` API，如下所示：

```java
 @Bean
 public FileWatcherProperties fileWatcherProperties(ConfigurableEnvironment environment) {
     return Binder.get(environment)
             .bind("file-watcher", Bindable.of(FileWatcherProperties.class))
             .orElse(new FileWatcherProperties());
 }
```

### 5.4.2 FileSystemWatcher优雅启停问题

> 如何在`Spring Boot`启动时自动调用其`start`方法，在退出时自动调用其`stop`方法？答案是自行实现`SmartLifecycle`接口。

```java
public class FileSystemWatcherFactoryBean implements
        FactoryBean<FileSystemWatcher>, SmartLifecycle, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemWatcherFactoryBean.class);

    private final AtomicBoolean started = new AtomicBoolean(false);

    private ApplicationContext applicationContext;

    @Override
    public FileSystemWatcher getObject() throws Exception {
        ImageListener imageListener = applicationContext.getBean(ImageListener.class);
        FileFilter imageFilter = applicationContext.getBean(FileFilter.class);
        FileWatcherProperties fileWatcherProperties = applicationContext.getBean(FileWatcherProperties.class);
        final FileSystemWatcher fileSystemWatcher = new FileSystemWatcher(fileWatcherProperties);
        fileSystemWatcher.addListener(imageListener);
        fileSystemWatcher.setFileFilter(imageFilter);
        return fileSystemWatcher;
    }

    @Override
    public Class<?> getObjectType() {
        return FileSystemWatcher.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void start() {
        if (started.compareAndSet(false, true)) {
            try {
                ((FileSystemWatcher) applicationContext.getBean(getObjectType())).start();
                logger.info("start FileSystemWatcher success");
            } catch (Exception e) {
                logger.error("start FileSystemWatcher error", e);
                started.set(false);
            }
        }
    }

    @Override
    public void stop() {
        if (started.compareAndSet(true, false)) {
            try {
                ((FileSystemWatcher) applicationContext.getBean(getObjectType())).stop();
                logger.info("stop FileSystemWatcher success");
            } catch (Exception e) {
                logger.error("stop FileSystemWatcher error", e);
                started.set(true);
            }
        }
    }

    @Override
    public boolean isRunning() {
        return started.get();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
```

### 5.4.3 acceptedStrategy为什么要设计为`Map<MatchingStrategy, Set<String>>`类型

如果针对`MatchingStrategy`和`patterns`设计为两个属性比较麻烦，而且后者与前者还有关联关系, 因此设计为`Map<MatchingStrategy, Set<String>>`类型。此外，为`acceptedStrategy`设定一个默认值为`Map.of(ANY, Set.of())`似乎更加合理而不是`null`，如下：

```java
private Map<MatchingStrategy, Set<String>> acceptedStrategy = Map.of(ANY, Set.of());
```

的确如此，但由于`Spring Boot`原生的`Relaxed Bingding`机制会针对`Map`类型有一个**merge**操作，这就导致最终绑定后`acceptedStrategy`的size为2，这是要避免的，如下：

```java
 /**
  * Sets the accepted strategy for file matching.
  * If the accepted strategy contains the {@link io.github.dk900912.filewatcher.filter.MatchingStrategy#ANY} key,
  * it will be filtered out first. After filtering, the accepted strategy must contain exactly one key, and the
  * corresponding value must not be empty.
  *
  * @param acceptedStrategy the accepted strategy map
  * @throws IllegalArgumentException if the accepted strategy is null, empty, contains more than one key after filtering,
  *                                  or the value for the key is empty
  */
 public void setAcceptedStrategy(Map<MatchingStrategy, Set<String>> acceptedStrategy) {
     Assert.isTrue(acceptedStrategy != null && !acceptedStrategy.isEmpty(),
             "AcceptedStrategy must not be null or empty");
     boolean onlyAny = acceptedStrategy.keySet().stream().allMatch(ANY::equals);
     if (!onlyAny) {
         Map<MatchingStrategy, Set<String>> filteredStrategy = acceptedStrategy.entrySet().stream()
                 .filter(entry -> !ANY.equals(entry.getKey()))
                 .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
         Assert.isTrue(filteredStrategy.size() == 1,
                 "AcceptedStrategy must contain exactly one key after filtering out ANY");
         Assert.isTrue(filteredStrategy.values().stream().noneMatch(Set::isEmpty),
                 "AcceptedStrategy must contain non-empty value for the key");
         this.acceptedStrategy = filteredStrategy;
     } else {
         this.acceptedStrategy = acceptedStrategy;
     }
 }
```

最后，来看看**merge**的大概流程：
```java
 private AggregateBinder<?> getAggregateBinder(Bindable<?> target, Context context) {
     Class<?> resolvedType = target.getType().resolve(Object.class);
     if (Map.class.isAssignableFrom(resolvedType)) {
         return new MapBinder(context);
     }
     if (Collection.class.isAssignableFrom(resolvedType)) {
         return new CollectionBinder(context);
     }
     if (target.getType().isArray()) {
         return new ArrayBinder(context);
     }
     return null;
 }

@Override
protected Map<Object, Object> merge(Supplier<Map<Object, Object>> existing, Map<Object, Object> additional) {
   Map<Object, Object> existingMap = getExistingIfPossible(existing);
   if (existingMap == null) {
      return additional;
   }
   try {
      existingMap.putAll(additional);
      return copyIfPossible(existingMap);
   }
   catch (UnsupportedOperationException ex) {
      Map<Object, Object> result = createNewMap(additional.getClass(), existingMap);
      result.putAll(additional);
      return result;
   }
}
```
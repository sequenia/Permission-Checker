# Permission-Checker
Библиотека, созданная с целью упрощения работы с Runtime Permissions.

# Использование
Сначала необходимо зарегистировать PermissionChecker как поле класса в фрагменте или активити, используя расширение registerPermissionChecker
```kotlin
class ExampleFragment: Fragment {
    private val permissionChecker = registerPermissionChecker()
}
```

Используя permissionChecker, можно запросить множество разрешений
```kotlin
private fun checkPermissions() {
    val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )
    permissionChecker.check(permissions) {
        // Будет вызван, если были даны все запрашиваемые разрешения
        onAllGranted {
        }

        // Будет вызван, если не было дано, хоть одно из запрашиваемых разрешений
        onAnyDenied { deniedPermissions ->
        }

        // Будет вызван, если хоть одному из запрашиваемых разрешений было отказано навсегда
        onDeniedPermanent { deniedPermissions ->
        }
    }
}
```

Можно обработать показ Rationale
```kotlin
private fun checkPermissions() {
    val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )
    permissionChecker.check(permissions) {
        onAllGranted {
        }

        onAnyDenied { deniedPermissions ->
        }

        onDeniedPermanent { deniedPermissions ->
        }

        // Будет вызван если необходимо показать rationale хоть для одного разрешения
        onShowRationale {
            showRationale(::acceptRationale)
        }
    }
}

private fun showRationale(acceptRationale: () -> Unit) {
    // Вызвать acceptRationale после принятия rationale пользователем, чтобы продолжить запрос разрешений
    acceptRationale()
}
```

Можно реагировать на отказ\принятие каждого разрешения отдельно
```kotlin
private fun checkPermissions() {
    permissionChecker.check {
        permission(Manifest.permission.RECORD_AUDIO) {
            onGranted {
            }

            onDenied {
            }
        }

        permission(Manifest.permission.CAMERA) {
            onGranted {
            }

            onDenied {
            }
        }
    }
}
```

Также библиотекой можно пользоваться без DSL
```kotlin
permissionChecker.clear()

permissionChecker.setOnAllGrantedCallback {
}

permissionChecker.setOnAnyDeniedCallback {
}

val audioPermission = PermissionRequestBuilder(Manifest.permission.RECORD_AUDIO).apply {
    onDenied = {
    }

    onGranted = {
    }
}.build()

val permissionRequests = listOf(audioPermission)
permissionChecker.addPermissionRequests(permissionRequests)
permissionChecker.check()
```


# Подключение
Сначала необходимо добавить jitpack репозиторий в build.gradle проекта
```groovy
allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
        // Добавьте эту строку
        maven { url 'https://jitpack.io' }
    }
}
```

Затем подключить библиотеку в build.gradle модуля
```groovy
dependencies {
    ...
    implementation 'com.github.sequenia:Permission-Checker:0.1'
    ...
}
```

# Passkey（WebAuthn）

## 1. Passkey

在注册过程中，用户设备会为网站生成一对 **公私钥**：

* **私钥**：保存在设备或系统密钥链中（无法导出）
* **公钥**：上传并存储在网站服务器

注册成功后服务器就能用公钥验证未来的登录签名，实现无密码认证。

---

## 2. 注册流程

整体流程：

1. 前端向后端请求注册参数（challenge、公钥算法等）
2. 后端生成参数并返回给前端
3. 前端调用 `navigator.credentials.create()` 创建 Passkey 凭证
4. 用户通过指纹、人脸或安全密钥确认
5. 前端把凭证（attestation）上传给后端
6. 后端验证有效性并存储公钥、credential_id、sign_count

---

## 3. 详细注册流程

### 3.1 前端发起注册请求

前端请求后端生成注册用的 publicKey options：

```javascript
const res = await fetch('/webauthn/register/options', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: 'alice@example.com' })
});

const options = await res.json();
```

---

### 3.2 后端生成注册参数

后端生成：

* 随机 `challenge`
* `rp`（网站信息）
* `user` 信息
* `pubKeyCredParams`（公钥算法）
* `attestation` 类型

示例响应：

```json
{
  "publicKey": {
    "challenge": "KQ6yT...lM",
    "rp": { "name": "My Awesome App", "id": "example.com" },
    "user": {
      "id": "YWxpY2VAZXhhbXBsZS5jb20=",
      "name": "alice@example.com",
      "displayName": "Alice"
    },
    "pubKeyCredParams": [{ "type": "public-key", "alg": -7 }],
    "timeout": 60000,
    "attestation": "direct"
  }
}
```

---

### 3.3 前端调用 WebAuthn API 创建 Passkey

```javascript
const credential = await navigator.credentials.create(options);
```

浏览器会弹出指纹、人脸或安全密钥确认对话框。

---

### 3.4 前端将创建结果发送给后端

```javascript
await fetch('/webauthn/register/verify', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    id: credential.id,
    rawId: arrayBufferToBase64(credential.rawId),
    type: credential.type,
    response: {
      attestationObject: arrayBufferToBase64(credential.response.attestationObject),
      clientDataJSON: arrayBufferToBase64(credential.response.clientDataJSON)
    }
  })
});
```

---

### 3.5 后端验证凭证并存储公钥

后端需要验证：

* challenge 是否与前端发起的一致
* origin 是否合法（如 [https://example.com）](https://example.com）)
* attestation 签名是否合法
* 提取 `credentialPublicKey` 与 `credential_id`

并存储以下字段：

| 字段            | 说明               |
| ------------- | ---------------- |
| credential_id | 用户 Passkey 的唯一标识 |
| public_key    | 公钥，用来验证签名        |
| sign_count    | 防重放计数器           |
| user_id       | 所属用户             |

---

## 4. 时序图

```
[User] → [Frontend] → [Backend] → [Browser] → [Authenticator]
   ↓          ↓           ↓           ↓             ↓
点击注册 → 请求注册参数 → 生成Challenge → WebAuthn.create() → 指纹/FaceID 验证
   ↓          ↓           ↓           ↓             ↓
← 参数返回 ← Challenge记录 ← 凭证结果 ← 凭证上传 ← 验证并保存公钥
```

---

## 5. 必须存储的关键字段

### 5.1 credential_id

* 标识该 Passkey 是哪台设备生成的
* 登录时用它查找对应的公钥

### 5.2 sign_count

* 防止重放攻击
* 每次登录签名后递增，服务器需验证并更新

---

# 6. Passkey（WebAuthn）登录 / 验证流程

Passkey 登录流程使用注册时保存的公钥进行签名验证，实现无密码登录。

## 6.1 登录流程

1. 前端请求登录（assertion）参数
2. 后端生成 challenge 和允许的 credential_id 列表
3. 前端调用 `navigator.credentials.get()`
4. 用户验证（指纹/FaceID/安全密钥）
5. 前端将 assertion 签名上传给后端
6. 后端验证签名、校验 sign_count 并完成登录

---

## 6.2 前端请求登录参数

```javascript
const res = await fetch('/webauthn/auth/options', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: 'alice@example.com' })
});

const options = await res.json();
```

---

## 6.3 后端生成登录参数

后端根据用户查找所有 credential_id

示例返回：

```json
{
  "publicKey": {
    "challenge": "oCbn...S8",
    "timeout": 60000,
    "rpId": "example.com",
    "allowCredentials": [
      {
        "id": "id_iphone_123",
        "type": "public-key"
      },
      {
        "id": "id_macbook_456",
        "type": "public-key"
      }
    ],
    "userVerification": "preferred"
  }
}
```

---

## 6.4 前端调用 WebAuthn API 获取登录凭证

```javascript
const assertion = await navigator.credentials.get(options);
```

系统会触发生物识别或安全密钥操作。

---

## 6.5 前端上传 assertion 给后端验证

```javascript
await fetch('/webauthn/auth/verify', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    id: assertion.id,
    rawId: arrayBufferToBase64(assertion.rawId),
    type: assertion.type,
    response: {
      authenticatorData: arrayBufferToBase64(assertion.response.authenticatorData),
      clientDataJSON: arrayBufferToBase64(assertion.response.clientDataJSON),
      signature: arrayBufferToBase64(assertion.response.signature),
      userHandle: arrayBufferToBase64(assertion.response.userHandle)
    }
  })
});
```

---

## 6.6 后端验证 assertion

后端验证以下内容：

* challenge 是否匹配
* origin 是否合法
* 用该 credential_id 对应的公钥验证 signature
* 检查 sign_count 是否递增，以防重放攻击

验证成功后更新 sign_count，并完成登录。

---

# 7.总结 

```
用户 → 前端 → 后端 → 浏览器/设备

【注册】
1. 前端请求注册参数
2. 后端生成 challenge
3. 前端调用 WebAuthn.create
4. 设备生成公私钥，返回凭证
5. 前端上传 credential
6. 后端验证并保存（credential_id、公钥、sign_count）

【登录】
1. 前端请求登录参数
2. 后端生成 challenge 和 allowCredentials
3. 前端调用 WebAuthn.get
4. 设备使用私钥签名
5. 前端上传 assertion
6. 后端验证签名并更新 sign_count 完成登录
```



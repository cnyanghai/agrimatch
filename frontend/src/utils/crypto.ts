export async function generateKeyPair(): Promise<CryptoKeyPair> {
  return await window.crypto.subtle.generateKey(
    {
      name: 'RSASSA-PKCS1-v1_5',
      modulusLength: 2048,
      publicExponent: new Uint8Array([1, 0, 1]),
      hash: 'SHA-256'
    },
    true,
    ['sign', 'verify']
  )
}

export async function exportPrivateKey(key: CryptoKey): Promise<string> {
  const exported = await window.crypto.subtle.exportKey('pkcs8', key)
  return btoa(String.fromCharCode(...new Uint8Array(exported)))
}

export async function importPrivateKey(pem: string): Promise<CryptoKey> {
  const binaryDerString = atob(pem)
  const binaryDer = new Uint8Array(binaryDerString.length)
  for (let i = 0; i < binaryDerString.length; i++) {
    binaryDer[i] = binaryDerString.charCodeAt(i)
  }

  return await window.crypto.subtle.importKey(
    'pkcs8',
    binaryDer.buffer,
    {
      name: 'RSASSA-PKCS1-v1_5',
      hash: 'SHA-256'
    },
    true,
    ['sign']
  )
}

export async function signData(data: string, privateKey: CryptoKey): Promise<string> {
  const encoder = new TextEncoder()
  const signature = await window.crypto.subtle.sign(
    'RSASSA-PKCS1-v1_5',
    privateKey,
    encoder.encode(data)
  )
  return btoa(String.fromCharCode(...new Uint8Array(signature)))
}

export async function verifySignature(data: string, signature: string, publicKey: CryptoKey): Promise<boolean> {
  const encoder = new TextEncoder()
  const signatureBytes = Uint8Array.from(atob(signature), c => c.charCodeAt(0))

  return await window.crypto.subtle.verify(
    'RSASSA-PKCS1-v1_5',
    publicKey,
    signatureBytes,
    encoder.encode(data)
  )
}

export async function generateAESKey(): Promise<CryptoKey> {
  return await window.crypto.subtle.generateKey(
    {
      name: 'AES-GCM',
      length: 256
    },
    true,
    ['encrypt', 'decrypt']
  )
}

export async function encryptMessage(message: string, key: CryptoKey): Promise<{ iv: number[], data: number[] }> {
  const encoder = new TextEncoder()
  const iv = window.crypto.getRandomValues(new Uint8Array(12))
  const encrypted = await window.crypto.subtle.encrypt(
    { name: 'AES-GCM', iv },
    key,
    encoder.encode(message)
  )
  return {
    iv: Array.from(iv),
    data: Array.from(new Uint8Array(encrypted))
  }
}

export async function decryptMessage(encryptedData: { iv: number[], data: number[] }, key: CryptoKey): Promise<string> {
  const iv = new Uint8Array(encryptedData.iv)
  const data = new Uint8Array(encryptedData.data)
  const decrypted = await window.crypto.subtle.decrypt(
    { name: 'AES-GCM', iv },
    key,
    data
  )
  const decoder = new TextDecoder()
  return decoder.decode(decrypted)
}

export async function hashFile(file: File): Promise<string> {
  const buffer = await file.arrayBuffer()
  const hashBuffer = await window.crypto.subtle.digest('SHA-256', buffer)
  const hashArray = Array.from(new Uint8Array(hashBuffer))
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('')
}

export async function hashString(str: string): Promise<string> {
  const encoder = new TextEncoder()
  const hashBuffer = await window.crypto.subtle.digest('SHA-256', encoder.encode(str))
  const hashArray = Array.from(new Uint8Array(hashBuffer))
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('')
}

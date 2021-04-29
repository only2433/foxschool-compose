package com.littlefox.app.foxschool.enc

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec


class SimpleCrypto
{
    @Throws(Exception::class)
    fun encrypt(cleartext : String) : String
    {
        val rawKey = getRawKey(SEED.toByteArray())
        val result = encrypt(rawKey, cleartext.toByteArray())
        return toHex(result)
    }

    @Throws(Exception::class)
    fun decrypt(encrypted : String) : String
    {
        val rawKey = getRawKey(SEED.toByteArray())
        val enc = toByte(encrypted)
        val result = decrypt(rawKey, enc)
        return String(result)
    }

    fun toHex(txt : String) : String
    {
        return toHex(txt.toByteArray())
    }

    fun fromHex(hex : String) : String
    {
        return String(toByte(hex))
    }

    fun toByte(hexString : String) : ByteArray
    {
        val len = hexString.length / 2
        val result = ByteArray(len)
        for(i in 0 until len) result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).toByte()
        return result
    }

    fun toHex(buf : ByteArray?) : String
    {
        if(buf == null) return ""
        val result = StringBuffer(2 * buf.size)
        for(i in buf.indices)
        {
            appendHex(result, buf[i])
        }
        return result.toString()
    }

    companion object
    {
        private const val SEED = "littlefox*&70"
        private var sSimpleCrypto : SimpleCrypto? = null
        fun getInstance() : SimpleCrypto?
        {
            if(sSimpleCrypto == null)
            {
                sSimpleCrypto = SimpleCrypto()
            }
            return sSimpleCrypto
        }

        @Throws(Exception::class)
        private fun getRawKey(seed : ByteArray) : ByteArray
        {
            val kgen = KeyGenerator.getInstance("AES")
            val sr = SecureRandom.getInstance("SHA1PRNG")
            sr.setSeed(seed)
            kgen.init(128, sr) // 192 and 256 bits may not be available
            val skey = kgen.generateKey()
            return skey.encoded
        }

        @Throws(Exception::class)
        private fun encrypt(raw : ByteArray, clear : ByteArray) : ByteArray
        {
            val skeySpec = SecretKeySpec(raw, "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
            return cipher.doFinal(clear)
        }

        @Throws(Exception::class)
        private fun decrypt(raw : ByteArray, encrypted : ByteArray) : ByteArray
        {
            val skeySpec = SecretKeySpec(raw, "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, skeySpec)
            return cipher.doFinal(encrypted)
        }

        private const val HEX = "0123456789ABCDEF"
        private fun appendHex(sb : StringBuffer, b : Byte)
        {
            sb.append(HEX[b.toInt() shr 4 and 0x0f]).append(HEX[b.toInt() and 0x0f])
        }
    }
}
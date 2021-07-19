package com.littlefox.app.foxschool.enc

import android.util.Base64
import java.io.UnsupportedEncodingException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object SimpleCrypto
{
    var ivBytes = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
    private const val SEED = "littlefox*&70123"


    //AES256 암호화
    @Throws(
        UnsupportedEncodingException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        InvalidAlgorithmParameterException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    fun encode(str : String) : String
    {
        val textBytes = str.toByteArray(charset("UTF-8"))
        val ivSpec : AlgorithmParameterSpec = IvParameterSpec(ivBytes)
        val newKey = SecretKeySpec(SEED.toByteArray(charset("UTF-8")), "AES")
        var cipher : Cipher? = null
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec)
        return Base64.encodeToString(cipher.doFinal(textBytes), 0)
    }

    //AES256 복호화
    @Throws(
        UnsupportedEncodingException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        InvalidAlgorithmParameterException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )

    fun decode(str : String?) : String
    {
        val textBytes = Base64.decode(str, 0)        //byte[] textBytes = str.getBytes("UTF-8");
        val ivSpec : AlgorithmParameterSpec = IvParameterSpec(ivBytes)
        val newKey = SecretKeySpec(SEED.toByteArray(charset("UTF-8")), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec)
        return String(cipher.doFinal(textBytes), charset("UTF-8"))
    }
}
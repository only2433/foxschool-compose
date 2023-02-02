package com.littlefox.app.foxschool.`object`.result.main

import com.google.gson.annotations.SerializedName

class CompanyInformationResult
{
    @SerializedName("ceo")
    private val ceo = ""

    @SerializedName("address")
    private val address = ""

    @SerializedName("brn")
    private val brn = ""

    @SerializedName("mosrn")
    private val mosrn = ""

    /**
     * 전자 출판물 인증
     * @return 해당 텍스트
     */
    @SerializedName("ecn_number")
    private val ecn_number = ""

    @SerializedName("phone")
    private val phone = ""

    @SerializedName("fax")
    private val fax = ""

    fun getCEO() : String = ceo

    fun getAddress() : String = address

    /**
     * 사업자 등록 번호
     * @return 해당 텍스트
     */
    fun getCompanyRegistrationNumber() : String = brn

    /**
     * 통신 판매업 신고
     * @return 해당 텍스트
     */
    fun getReportCompany() : String = mosrn

    fun getElectronicPublishingCertification() : String = ecn_number

    fun getPhoneNumber() : String = phone

    fun getFaxNumber() : String = fax
}
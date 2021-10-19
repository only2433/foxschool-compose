package com.littlefox.app.foxschool.`object`.result.main

class CompanyInformationResult
{
    private val ceo = ""
    private val address = ""
    private val brn = ""
    private val mosrn = ""

    /**
     * 전자 출판물 인증
     * @return 해당 텍스트
     */
    private val ecn_number = ""
    private val phone = ""
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
package com.littlefox.app.foxschool.object.result.main;

public class CompanyInformationResult
{
    private String ceo = "";
    private String address = "";
    private String brn = "";
    private String mosrn = "";
    private String ecn_number = "";
    private String phone = "";
    private String fax = "";

    public String getCEO()
    {
        return ceo;
    }

    public String getAddress()
    {
        return address;
    }

    /**
     * 사업자 등록 번호
     * @return 해당 텍스트
     */
    public String getCompanyRegistrationNumber()
    {
        return brn;
    }

    /**
     * 통신 판매업 신고
     * @return 해당 텍스트
     */
    public String getReportCompany()
    {
        return mosrn;
    }

    /**
     * 전자 출판물 인증
     * @return 해당 텍스트
     */
    public String getElectronicPublishingCertification()
    {
        return ecn_number;
    }

    public String getPhoneNumber()
    {
        return phone;
    }

    public String getFaxNumber()
    {
        return fax;
    }
}

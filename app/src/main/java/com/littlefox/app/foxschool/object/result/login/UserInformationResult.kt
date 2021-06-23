package com.littlefox.app.foxschool.`object`.result.login

import java.util.*

class UserInformationResult
{
    private var current_user_id : String = ""
    private var country_code : String = ""
    private var expire_date : String = ""
    private var remaining_day : Int = 0
    private var mobile_url : String = ""
    private var users : ArrayList<UserInformation> = ArrayList<UserInformation>();

    val currentUserNickName : String
        get()
        {
            var result = ""
            for(i in users!!.indices)
            {
                if(current_user_id == users[i].getID())
                {
                    result = users[i].getNickName()
                }
            }
            return result
        }

    fun getCurrentUserID() : String
    {
        return current_user_id
    }

    fun getCountryCode() : String
    {
        return country_code
    }

    fun getExpireDate() : String
    {
        return expire_date
    }

    fun getRemainingDay() : Int
    {
        return remaining_day
    }

    fun getMobileUrlPrefix() : String
    {
        return mobile_url
    }

    fun getUserInformationList() : ArrayList<UserInformation>
    {
        return users;
    }

    inner class UserInformation
    {
        private val id = ""
        private val nickname = ""
        private val avatar_image_url = ""
        private val is_custom_avatar = ""
        val isCustomAvatar : Boolean
            get()
            {
                if(is_custom_avatar == "Y")
                    return true
                else
                    return false
            }

        fun getID() : String
        {
            return id;
        }

        fun getNickName() : String
        {
            return nickname;
        }

        fun getThumbnail() : String
        {
            return avatar_image_url;
        }
    }
}
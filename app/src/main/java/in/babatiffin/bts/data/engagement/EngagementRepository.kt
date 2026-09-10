package com.babatiffin.bts.data.engagement

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Serializable data class NotificationItem(val id:String,@SerialName("user_id")val userId:String,@SerialName("event_key")val eventKey:String,val channel:String,val title:String,val body:String?=null,val metadata:JsonObject=JsonObject(emptyMap()),@SerialName("read_at")val readAt:String?=null,@SerialName("created_at")val createdAt:String)
@Serializable data class ReferralCode(val id:String,@SerialName("user_id")val userId:String,val code:String)
@Serializable data class ReferralReward(val id:String,@SerialName("referrer_id")val referrerId:String,@SerialName("referred_id")val referredId:String,val amount:Double,val status:String,@SerialName("created_at")val createdAt:String)

interface EngagementRepository { suspend fun notifications(userId:String):List<NotificationItem>;suspend fun markRead(id:String,userId:String);suspend fun referralCode(userId:String):ReferralCode?;suspend fun rewards(userId:String):List<ReferralReward> }
class SupabaseEngagementRepository(private val client:SupabaseClient):EngagementRepository{
 override suspend fun notifications(userId:String)=client.from("notifications").select{filter{eq("user_id",userId)}}.decodeList<NotificationItem>().sortedByDescending(NotificationItem::createdAt)
 override suspend fun markRead(id:String,userId:String){val format=SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.US);format.timeZone=TimeZone.getTimeZone("UTC");client.from("notifications").update({set("read_at",format.format(Date()))}){filter{eq("id",id);eq("user_id",userId)}}}
 override suspend fun referralCode(userId:String)=client.from("referral_codes").select{filter{eq("user_id",userId)}}.decodeList<ReferralCode>().firstOrNull()
 override suspend fun rewards(userId:String)=client.from("referral_rewards").select{filter{eq("referrer_id",userId)}}.decodeList<ReferralReward>().sortedByDescending(ReferralReward::createdAt)
}

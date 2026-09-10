package com.babatiffin.bts.feature.engagement

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babatiffin.bts.data.engagement.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EngagementState(val loading:Boolean=false,val userId:String?=null,val notifications:List<NotificationItem> = emptyList(),val code:ReferralCode?=null,val rewards:List<ReferralReward> = emptyList(),val error:String?=null)
class EngagementViewModel(private val repository:EngagementRepository?):ViewModel(){private val mutable=MutableStateFlow(EngagementState());val state=mutable.asStateFlow();fun load(userId:String?){if(userId==null||userId==mutable.value.userId)return;viewModelScope.launch{mutable.value=EngagementState(true,userId);runCatching{EngagementState(false,userId,repository?.notifications(userId).orEmpty(),repository?.referralCode(userId),repository?.rewards(userId).orEmpty())}.onSuccess{mutable.value=it}.onFailure{mutable.value=EngagementState(false,userId,error="Notifications and referrals could not be loaded.")}}}fun read(item:NotificationItem){val user=mutable.value.userId?:return;viewModelScope.launch{repository?.markRead(item.id,user);mutable.value=mutable.value.copy(notifications=mutable.value.notifications.map{if(it.id==item.id)it.copy(readAt="read")else it})}}}

@Composable fun NotificationsScreen(state:EngagementState,onRead:(NotificationItem)->Unit){LazyColumn(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Text("Notifications",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);if(state.loading)CircularProgressIndicator();state.error?.let{Text(it)}};if(state.notifications.size==0)item{Text("No notifications yet.")}else items(state.notifications.size){i->val n=state.notifications[i];Card(onClick={onRead(n)},modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){Text(n.title,fontWeight=if(n.readAt==null)FontWeight.Bold else FontWeight.Normal);n.body?.let{Text(it)};Text(if(n.readAt==null)"Unread · Tap to mark read" else "Read",style=MaterialTheme.typography.bodySmall)}}}}}
@Composable fun ReferralsScreen(state:EngagementState){val context=LocalContext.current;val code=state.code?.code;LazyColumn(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Text("Refer & earn",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);if(code==null)Text("Referral code is not available for this account.")else{Text("Your code",style=MaterialTheme.typography.titleMedium);Text(code,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Button({val intent=Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_TEXT,"Join BTS Baba Tiffin Services with my referral code: $code")};context.startActivity(Intent.createChooser(intent,"Share referral code"))}){Text("Share code")}};HorizontalDivider();Text("Rewards",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)};if(state.rewards.size==0)item{Text("No referral rewards yet.")}else items(state.rewards.size){i->val r=state.rewards[i];Card(Modifier.fillMaxWidth()){Row(Modifier.padding(12.dp).fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("₹${r.amount.toInt()}");Text(r.status)}}}}}

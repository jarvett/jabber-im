package cn.xpride.framework.application.handler;

import cn.xpride.framework.application.biz.ChatBizService;
import cn.xpride.framework.application.dto.*;
import cn.xpride.framework.application.model.*;
import cn.xpride.framework.application.ws.WsHandler;
import cn.xpride.framework.base.*;
import cn.xpride.framework.utility.FastJsonUtil;
import cn.xpride.framework.utility.MyStringUtil;
import cn.xpride.framework.utility.RegexUtil;

import java.util.List;
import io.github.jarvett.core.ioc.annotation.JuncoManaged;
import io.github.jarvett.web.JuncoHttpRequest;
import jakarta.ws.rs.*;
import lombok.RequiredArgsConstructor;
import cn.xpride.framework.base.ChatJson;

@JuncoManaged
@RequiredArgsConstructor
@Path("/im")
public class ChatHandler extends AppWebBaseHandler {

    private final ChatBizService chatBizService;
    private final WsHandler wsHandler;

    @GET
    @Path("getPhoneCode")
    /** 获取手机验证码。 */
    public String getPhoneCode(JuncoHttpRequest http) throws HandlerException {
        String phone = required(http, "phone");
        HandlerExceptionHelper.throwException(!RegexUtil.isMobilePhone(phone), "手机号格式错误");
        return ChatJson.write(new PhoneCodeDto(MyStringUtil.hidePhone(phone)));
    }

    @GET
    @Path("getToken")
    /** 根据手机号和验证码获取token。 */
    public String getToken(JuncoHttpRequest http) throws HandlerException {
        String phone = required(http, "phone");
        String code = required(http, "code");
        HandlerExceptionHelper.throwException(!"123456".equals(code), "手机验证码错误");
        Long id = chatBizService.findOrCreateSubscriberByPhone(phone);
        MyToken myToken = MyTokenUtil.saveToken(id, UserType.APP_USER.getUserType(), UserType.APP_USER.getTimeout());
        return ChatJson.write(new TokenDto(id, myToken.getToken(), myToken.getExpiresAtStamp()));
    }

    @GET
    @Path("getMyProfile")
    /** 获取个人资料。 */
    public String getMyProfile(JuncoHttpRequest http) throws HandlerException {

        Long userId = getMyToken(http).getUserId();
        SubscriberModel model = chatBizService.getSubscriber(userId);
        return ChatJson.write(new SubscriberModelDto(model));
    }

    @GET
    @Path("getMyProfileByPhone")
    /** 使用手机号获取个人资料。 */
    public String getMyProfileByPhone(JuncoHttpRequest http) throws HandlerException {
        String phone = required(http, "phone");
        Long userId = getMyToken(http).getUserId();
        HandlerExceptionHelper.throwException(!RegexUtil.isMobilePhone(phone),"手机号格式错误");
        SubscriberModel model = chatBizService.getMyProfileByPhone(userId,phone);
        return ChatJson.write(new SubscriberModelDto(model));
    }

    @POST
    @Path("changeAvatarAndNickName")
    /** 换个马甲。 */
    public String changeAvatarAndNickName(JuncoHttpRequest http) throws HandlerException {

        Long userId = getMyToken(http).getUserId();
        chatBizService.changeAvatarAndNickName(userId);
        return ChatJson.write(BaseResponse.OK);
    }

    @POST
    @Path("changeIntro")
    /** 修改个性签名。 */
    public String changeIntro(JuncoHttpRequest http) throws HandlerException {
        String intro = required(http, "intro");
        Long userId = getMyToken(http).getUserId();
        HandlerExceptionHelper.throwException(MyStringUtil.isEmpty(intro),"签名不能为空");
        chatBizService.changeIntro(userId,intro);
        return ChatJson.write(BaseResponse.OK);
    }

    @GET
    @Path("getRandomStranger")
    /** 获取5个随机陌生人来聊天。 */
    public String getRandomStranger(JuncoHttpRequest http) throws HandlerException {

        Long userId = getMyToken(http).getUserId();
        List<SubscriberModel> list = chatBizService.findRandomStranger(userId);
        return ChatJson.write(new SubscriberModelListDto(list));
    }

    @POST
    @Path("createPrivateDialogue")
    /** 和某个用户创建私聊会话。 */
    public String createPrivateDialogue(JuncoHttpRequest http) throws HandlerException {
        Long sid = longParam(http, "sid");
        Long userId = getMyToken(http).getUserId();
        DialogueModel dialogueModel = chatBizService.findOrCreatePrivateDialogue(userId, sid);
        return ChatJson.write(new DialogueModelDto(dialogueModel));
    }

    @POST
    @Path("createGroupDialogue")
    /** 和至少2个用户创建群聊会话。 */
    public String createGroupDialogue(JuncoHttpRequest http) throws HandlerException {
        List<Long> sidList = longList(http, "sidList");
        String groupName = required(http, "groupName");
        Long userId = getMyToken(http).getUserId();
        DialogueModel dialogueModel = chatBizService.createGroupDialogue(userId, sidList,groupName);
        return ChatJson.write(new DialogueModelDto(dialogueModel));
    }

    @GET
    @Path("getMyDialogueList")
    /** 获取我的聊天会话列表。 */
    public String getMyDialogueList(JuncoHttpRequest http) throws HandlerException {
        Integer pageNow = page(http, "pageNow");
        Long userId = getMyToken(http).getUserId();
        List<DialogueModel> dialogueModelList = chatBizService.getDialogueListBySubscriberId(userId, pageNow);
        return ChatJson.write(new DialogueModelListDto(dialogueModelList));
    }

    @GET
    @Path("getDialogueByChannelId")
    /** 通过通讯频道ID获取聊天会话详情。 */
    public String getDialogueByChannelId(JuncoHttpRequest http) throws HandlerException {
        Long channelId = longParam(http, "channelId");
        Long sid = getMyToken(http).getUserId();
        DialogueModel model = chatBizService.getDialogueByChannelId(sid,channelId);
        return ChatJson.write(new DialogueModelDto(model));
    }

    @GET
    @Path("getSubscriberModelListByChannelId")
    /** 通过通讯频道ID获取聊天用户资料。 */
    public String getSubscriberModelListByChannelId(JuncoHttpRequest http) throws HandlerException {
        Long channelId = longParam(http, "channelId");
        Long userId = getMyToken(http).getUserId();
        List<SubscriberModel> subscriberList = chatBizService.getSubscriberModelListByChannelId(userId,channelId);
        return ChatJson.write(new SubscriberModelListDto(subscriberList));
    }

    @POST
    @Path("sendMsgToChannel")
    /** 发送消息到通讯频道。 */
    public String sendMsgToChannel(JuncoHttpRequest http) throws HandlerException {
        MessageContentModel contentModel = content(http);
        Long channelId = longParam(http, "channelId");
        Long userId = getMyToken(http).getUserId();
        MessageContentRequest request = new MessageContentRequest();
        request.setSenderId(userId);
        request.setChannelId(channelId);
        request.setContentModel(contentModel);
        MessageContentResponse ret = chatBizService.sendMsgToChannel(request);
        List<Long> receiveIdList = ret.getReceiveIdList();
        ret.setReceiveIdList(null);
        receiveIdList.forEach(id -> wsHandler.sendMsgToUser(id, FastJsonUtil.toCompatibleJSON(ret)));
        return ChatJson.write(BaseResponse.OK);
    }

    @GET
    @Path("getMsgListByChannelId")
    /** 根据通讯频道ID获取通讯记录。 */
    public String getMsgListByChannelId(JuncoHttpRequest http) throws HandlerException {
        Long channelId = longParam(http, "channelId");
        Integer pageNow = page(http, "pageNow");
        Long userId = getMyToken(http).getUserId();
        List<ChannelMsgModel> list = chatBizService.getMsgListByChannelId(userId, channelId, pageNow);
        return ChatJson.write(new ChannelMsgModelDto(list));
    }


    @POST
    @Path("markRead")
    /** 确认当前用户实际展示的最新消息，返回服务端会话状态。 */
    public String markRead(JuncoHttpRequest http) throws HandlerException {
        Long channelId = longParam(http, "channelId");
        String messageId = required(http, "messageId");
        return ChatJson.write(new DialogueModelDto(chatBizService.markRead(getMyToken(http).getUserId(), channelId, messageId)));
    }
}

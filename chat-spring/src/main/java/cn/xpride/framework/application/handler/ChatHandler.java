package cn.xpride.framework.application.handler;

import cn.xpride.framework.application.biz.ChatBizService;
import cn.xpride.framework.application.dto.*;
import cn.xpride.framework.application.model.*;
import cn.xpride.framework.application.ws.WsHandler;
import cn.xpride.framework.base.*;
import cn.xpride.framework.utility.FastJsonUtil;
import cn.xpride.framework.utility.MyStringUtil;
import cn.xpride.framework.utility.RegexUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@Api(tags = "IM消息服务")
@RequestMapping(value = "im",produces = MediaType.APPLICATION_JSON_VALUE)
public class ChatHandler extends AppWebBaseHandler {

    @Autowired
    private ChatBizService chatBizService;
    @Autowired
    private WsHandler wsHandler;

    @GetMapping(value = "getPhoneCode")
    @ApiOperation("获取手机验证码")
    public PhoneCodeDto getPhoneCode(@ApiParam(value = "手机号",required = true) @RequestParam String phone) throws HandlerException {
        HandlerExceptionHelper.throwException(!RegexUtil.isMobilePhone(phone), "手机号格式错误");
        return new PhoneCodeDto(MyStringUtil.hidePhone(phone));
    }

    @GetMapping(value = "getToken")
    @ApiOperation("根据手机号和验证码获取token")
    public TokenDto getToken(@ApiParam(value = "手机号",required = true) @RequestParam String phone,
                             @ApiParam(value = "手机验证码",required = true) @RequestParam String code) throws HandlerException {
        HandlerExceptionHelper.throwException(!"123456".equals(code), "手机验证码错误");
        Long id = chatBizService.findOrCreateSubscriberByPhone(phone);
        MyToken myToken = MyTokenUtil.saveToken(id, UserType.APP_USER.getUserType(), UserType.APP_USER.getTimeout());
        return new TokenDto(id, myToken.getToken(), myToken.getExpiresAtStamp());
    }

    @GetMapping(value = "getMyProfile")
    @ApiOperation("获取个人资料")
    public SubscriberModelDto getMyProfile() throws HandlerException{
        Long userId = getMyToken().getUserId();
        SubscriberModel model = chatBizService.getSubscriber(userId);
        return new SubscriberModelDto(model);
    }

    @GetMapping(value = "getMyProfileByPhone")
    @ApiOperation("使用手机号获取个人资料")
    public SubscriberModelDto getMyProfileByPhone(@ApiParam(value = "手机号",required = true) @RequestParam String phone) throws HandlerException{
        Long userId = getMyToken().getUserId();
        HandlerExceptionHelper.throwException(!RegexUtil.isMobilePhone(phone),"手机号格式错误");
        SubscriberModel model = chatBizService.getMyProfileByPhone(userId,phone);
        return new SubscriberModelDto(model);
    }

    @PostMapping(value = "changeAvatarAndNickName")
    @ApiOperation("换个马甲")
    public BaseResponse changeAvatarAndNickName() throws HandlerException{
        Long userId = getMyToken().getUserId();
        chatBizService.changeAvatarAndNickName(userId);
        return BaseResponse.OK;
    }

    @PostMapping(value = "changeIntro")
    @ApiOperation("修改个性签名")
    public BaseResponse changeIntro(@ApiParam(value = "个性签名",required = true) @RequestParam String intro) throws HandlerException{
        Long userId = getMyToken().getUserId();
        HandlerExceptionHelper.throwException(MyStringUtil.isEmpty(intro),"签名不能为空");
        chatBizService.changeIntro(userId,intro);
        return BaseResponse.OK;
    }

    @GetMapping(value = "getRandomStranger")
    @ApiOperation("获取5个随机陌生人来聊天")
    public SubscriberModelListDto getRandomStranger() throws HandlerException {
        Long userId = getMyToken().getUserId();
        List<SubscriberModel> list = chatBizService.findRandomStranger(userId);
        return new SubscriberModelListDto(list);
    }

    @PostMapping(value = "createPrivateDialogue")
    @ApiOperation("和某个用户创建私聊会话")
    public DialogueModelDto createPrivateDialogue(@ApiParam(value = "私聊对象的SID",required = true) @RequestParam Long sid) throws HandlerException {
        Long userId = getMyToken().getUserId();
        DialogueModel dialogueModel = chatBizService.findOrCreatePrivateDialogue(userId, sid);
        return new DialogueModelDto(dialogueModel);
    }

    @PostMapping(value = "createGroupDialogue")
    @ApiOperation("和至少2个用户创建群聊会话")
    public DialogueModelDto createGroupDialogue(@ApiParam(value = "其他加入群聊用户sid的列表") @RequestParam List<Long> sidList,
                                                @ApiParam(value = "群名称") @RequestParam String groupName) throws HandlerException {
        Long userId = getMyToken().getUserId();
        DialogueModel dialogueModel = chatBizService.createGroupDialogue(userId, sidList,groupName);
        return new DialogueModelDto(dialogueModel);
    }

    @GetMapping(value = "getMyDialogueList")
    @ApiOperation("获取我的聊天会话列表")
    public DialogueModelListDto getMyDialogueList(@ApiParam(value = "页码",defaultValue = "1") @RequestParam(defaultValue = "1") Integer pageNow) throws HandlerException {
        Long userId = getMyToken().getUserId();
        List<DialogueModel> dialogueModelList = chatBizService.getDialogueListBySubscriberId(userId, pageNow);
        return new DialogueModelListDto(dialogueModelList);
    }

    @GetMapping(value = "getDialogueByChannelId")
    @ApiOperation("通过通讯频道ID获取聊天会话详情")
    public DialogueModelDto getDialogueByChannelId(@ApiParam(value = "通讯频道ID") @RequestParam Long channelId) throws HandlerException{
        Long sid = getMyToken().getUserId();
        DialogueModel model = chatBizService.getDialogueByChannelId(sid,channelId);
        return new DialogueModelDto(model);
    }

    @GetMapping(value = "getSubscriberModelListByChannelId")
    @ApiOperation("通过通讯频道ID获取聊天用户资料")
    public SubscriberModelListDto getSubscriberModelListByChannelId(@ApiParam(value = "通讯频道ID") @RequestParam Long channelId) throws HandlerException{
        Long userId = getMyToken().getUserId();
        List<SubscriberModel> subscriberList = chatBizService.getSubscriberModelListByChannelId(userId,channelId);
        return new SubscriberModelListDto(subscriberList);
    }

    @PostMapping(value = "sendMsgToChannel")
    @ApiOperation("发送消息到通讯频道")
    public BaseResponse sendMsgToChannel(@Valid MessageContentModel contentModel, @ApiParam(value = "通讯频道ID",required = true) @RequestParam Long channelId) throws HandlerException {
        Long userId = getMyToken().getUserId();
        MessageContentRequest request = new MessageContentRequest();
        request.setSenderId(userId);
        request.setChannelId(channelId);
        request.setContentModel(contentModel);
        MessageContentResponse ret = chatBizService.sendMsgToChannel(request);
        List<Long> receiveIdList = ret.getReceiveIdList();
        ret.setReceiveIdList(null);
        receiveIdList.forEach(id -> wsHandler.sendMsgToUser(id, FastJsonUtil.toCompatibleJSON(ret)));
        return BaseResponse.OK;
    }

    @GetMapping(value = "getMsgListByChannelId")
    @ApiOperation("根据通讯频道ID获取通讯记录")
    public ChannelMsgModelDto getMsgListByChannelId(@ApiParam(value = "通讯频道ID") @RequestParam Long channelId,
                                                    @ApiParam(value = "页码",defaultValue = "1") @RequestParam(defaultValue = "1") Integer pageNow) throws HandlerException {
        Long userId = getMyToken().getUserId();
        List<ChannelMsgModel> list = chatBizService.getMsgListByChannelId(userId, channelId, pageNow);
        return new ChannelMsgModelDto(list);
    }


    @PostMapping(value = "markRead")
    @ApiOperation("确认当前用户已读消息")
    public DialogueModelDto markRead(@RequestParam Long channelId, @RequestParam String messageId) throws HandlerException {
        return new DialogueModelDto(chatBizService.markRead(getMyToken().getUserId(), channelId, messageId));
    }
}

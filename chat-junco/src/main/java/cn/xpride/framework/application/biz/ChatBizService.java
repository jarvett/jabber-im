package cn.xpride.framework.application.biz;

import cn.xpride.framework.application.entity.Channel;
import cn.xpride.framework.application.entity.ChannelMsg;
import cn.xpride.framework.application.entity.Dialogue;
import cn.xpride.framework.application.entity.Subscriber;
import cn.xpride.framework.application.model.*;
import cn.xpride.framework.application.repository.ChannelMsgRepository;
import cn.xpride.framework.application.repository.ChannelRepository;
import cn.xpride.framework.application.repository.DialogueRepository;
import cn.xpride.framework.application.repository.SubscriberRepository;
import cn.xpride.framework.base.HandlerException;
import cn.xpride.framework.base.HandlerExceptionHelper;
import cn.xpride.framework.base.ImConstant;
import cn.xpride.framework.utility.*;
import com.alibaba.fastjson.JSONObject;
import io.github.jarvett.jdbc.annotation.JuncoTransactional;

import io.github.jarvett.core.ioc.annotation.JuncoManaged;
import lombok.RequiredArgsConstructor;
import java.util.concurrent.ExecutorService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@JuncoManaged
@RequiredArgsConstructor
public class ChatBizService {

    private final SubscriberRepository subscriberRepository;
    private final DialogueRepository dialogueRepository;
    private final ChannelRepository channelRepository;
    private final ChannelMsgRepository channelMsgRepository;
    private final ExecutorService threadPoolTaskExecutor;

    public SubscriberModel getSubscriber(Long userId) throws HandlerException {
        Subscriber item = subscriberRepository.findFirstById(userId);
        HandlerExceptionHelper.throwTokenExpiredIfNull(item);
        return new SubscriberModel(item.getId(), item.getNickname(), item.getAvatar(), item.getCreateTime(),item.getUniqueId(),item.getIntro());
    }

    public SubscriberModel getMyProfileByPhone(Long userId, String phone) throws HandlerException{
        String appId = "native";
        Subscriber item = subscriberRepository.findTopByAppIdAndUniqueId(appId,phone);
        HandlerExceptionHelper.throwExceptionIfNull(item,"用户不存在");
        return new SubscriberModel(item.getId(), item.getNickname(), item.getAvatar(), item.getCreateTime(),item.getUniqueId(),item.getIntro());
    }

    public void changeAvatarAndNickName(Long userId) {
        String avatarUrl = DummyUtil.getRandomAvatarUrl();
        String nickName = DummyUtil.getRandomNickName();
        subscriberRepository.updateAvatarAndNickName(userId,avatarUrl,nickName);
    }

    public void changeIntro(Long userId, String intro) {
        subscriberRepository.updateIntro(userId,intro);
    }

    public Long findOrCreateSubscriberByPhone(String phone) {
        String appId = "native";
        long now = System.currentTimeMillis();
        Subscriber subscriber = subscriberRepository.findTopByAppIdAndUniqueId(appId, phone);
        if (null == subscriber) {
            subscriber = new Subscriber();
            subscriber.setId(subscriberRepository.getNewSubscriberId());
            subscriber.setType(ImConstant.SUBSCRIBER_TYPE_NORMAL);
            subscriber.setAppId(appId);
            subscriber.setUniqueId(phone);
            subscriber.setNickname(DummyUtil.getRandomNickName());
            subscriber.setAvatar(DummyUtil.getRandomAvatarUrl());
            subscriber.setIntro("");
            subscriber.setCreateTime(now);
            subscriber.setUpdateTime(now);
            subscriber.setLastLoginTime(now);
            subscriberRepository.save(subscriber);
        } else {
            subscriberRepository.updateLaseLoginTimeById(subscriber.getId(),now);
        }
        return subscriber.getId();
    }

    public List<SubscriberModel> findRandomStranger(Long notInId) {
        List<Subscriber> list = subscriberRepository.randomSubscriber(notInId);
        return list.stream().map(item -> new SubscriberModel(item.getId(), item.getNickname(), item.getAvatar(), item.getCreateTime(),item.getUniqueId(),item.getIntro())).collect(Collectors.toList());
    }

    private ChannelModel findOrCreatePrivateChannel(Long fromSid, Long toSid) throws HandlerException {
        String joinSubscriberIds = LongUtil.joint(Arrays.asList(fromSid.toString(), toSid.toString()));
        Channel channel = channelRepository.getFirstByTypeAndJoinSubscriberIds(ImConstant.CHANNEL_TYPE_PRIVATE,joinSubscriberIds);
        if (null == channel) {
            long now = System.currentTimeMillis();
            channel = new Channel();
            channel.setId(channelRepository.getNewChannelId());
            channel.setType(ImConstant.CHANNEL_TYPE_PRIVATE);
            ChannelConfigModel configModel = new ChannelConfigModel();
            configModel.setSidList(Arrays.asList(fromSid, toSid));
            configModel.setType(channel.getType());
            channel.setChannelConfig(FastJsonUtil.toCompatibleJSON(configModel));
            channel.setJoinSubscriberIds(joinSubscriberIds);
            channel.setIsNeedAccess(ImConstant.NO);
            channel.setMsgCount(0);
            channel.setCreateTime(now);
            channel.setUpdateTime(now);
            channelRepository.save(channel);
        }
        return new ChannelModel(channel.getId(), channel.getType());
    }

    private Dialogue getPrivateDialogue(Long fromSid, Long toSid, Long channelId) {
        Dialogue dialogue = dialogueRepository.getBySubscriberIdAndChannelId(fromSid,channelId);
        if (null == dialogue) {
            Subscriber toSub = subscriberRepository.findFirstById(toSid);
            long now = System.currentTimeMillis();
            dialogue = new Dialogue();
            dialogue.setId(dialogueRepository.getNewDialogueId());
            dialogue.setSubscriberId(fromSid);
            dialogue.setChannelId(channelId);
            dialogue.setType(ImConstant.DIALOGUE_TYPE_CHAT);
            dialogue.setIsVisible(ImConstant.YES);
            dialogue.setShowOuter(ImConstant.YES);
            dialogue.setName(toSub.getNickname());
            dialogue.setAvatar(toSub.getAvatar());
            dialogue.setBrief(MyStringUtil.ellipsis("你已添加了" + toSub.getNickname() + ",可以开始聊天了"));
            dialogue.setLastTime(now);
            dialogue.setLastReadTime(now);
            dialogue.setUnreadCount(0);
            dialogueRepository.save(dialogue);
        }
        return dialogue;
    }

    public DialogueModel findOrCreatePrivateDialogue(Long fromSid, Long toSid) throws HandlerException {
        HandlerExceptionHelper.throwException(subscriberRepository.countById(toSid) < 1,"私聊对象不存在");
        HandlerExceptionHelper.throwException(fromSid.equals(toSid),"你真是无聊!");
        ChannelModel channelModel = findOrCreatePrivateChannel(fromSid, toSid);
        Long channelId = channelModel.getId();
        Dialogue dialogue = getPrivateDialogue(fromSid, toSid, channelId);
        return new DialogueModel(dialogue.getId(), channelId, dialogue.getType(), dialogue.getName(), dialogue.getAvatar(), dialogue.getBrief(), dialogue.getLastTime(), dialogue.getUnreadCount());
    }

    private DialogueModel getGroupDialogueModel(Long fromSid, Long channelId,ChannelConfigModel configModel,String brief) {
        Dialogue dialogue = dialogueRepository.getBySubscriberIdAndChannelId(fromSid,channelId);
        if (null == dialogue) {
            long now = System.currentTimeMillis();
            dialogue = new Dialogue();
            dialogue.setId(dialogueRepository.getNewDialogueId());
            dialogue.setSubscriberId(fromSid);
            dialogue.setType(ImConstant.DIALOGUE_TYPE_GROUP);
            dialogue.setChannelId(channelId);
            dialogue.setName(configModel.getGroupName());
            dialogue.setAvatar(configModel.getGroupAvatar());
            dialogue.setBrief(brief);
            dialogue.setUnreadCount(0);
            dialogue.setIsVisible(ImConstant.YES);
            dialogue.setShowOuter(ImConstant.YES);
            dialogue.setLastTime(now);
            dialogue.setLastReadTime(now);
            dialogueRepository.save(dialogue);
        }
        return new DialogueModel(dialogue.getId(), dialogue.getChannelId(), dialogue.getType(), dialogue.getName(), dialogue.getAvatar(), dialogue.getBrief(), dialogue.getLastTime(), dialogue.getUnreadCount());
    }

    public DialogueModel createGroupDialogue(Long fromSid, List<Long> sidList,String groupName) throws HandlerException {
        HandlerExceptionHelper.throwException(sidList.size() < 2, "至少邀请2人组成群聊");
        long now = System.currentTimeMillis();
        Channel channel = new Channel();
        channel.setId(channelRepository.getNewChannelId());
        channel.setType(ImConstant.CHANNEL_TYPE_GROUP);
        ChannelConfigModel configModel = new ChannelConfigModel();
        sidList.add(0, fromSid);
        configModel.setSidList(sidList);
        List<Subscriber> subscriberList = subscriberRepository.findAllByIdIn(sidList);
        HandlerExceptionHelper.throwException (subscriberList.size() != sidList.size(),"聊天对象不匹配,ID传错");
        List<String> avatars = subscriberList.stream().map(Subscriber::getAvatar).collect(Collectors.toList());
        configModel.setGroupAvatar(ImageUtil.getCombinationOfheadBase64(avatars));
        if (MyStringUtil.isEmpty(groupName)){
            groupName = "群聊(" + sidList.size() + ")";
        }
        configModel.setGroupName(groupName);
        configModel.setType(channel.getType());
        channel.setChannelConfig(FastJsonUtil.toCompatibleJSON(configModel));
        channel.setCreateTime(now);
        channel.setJoinSubscriberIds(fromSid.toString());
        channel.setUpdateTime(now);
        channel.setIsNeedAccess(ImConstant.NO);
        channel.setMsgCount(0);
        channelRepository.save(channel);

        String name = subscriberList.stream().map(Subscriber::getNickname).collect(Collectors.joining("、"));
        String brief = "你邀请了" + MyStringUtil.ellipsis(name)+ "等" + (sidList.size() - 1) + "人加入了群聊";
        return getGroupDialogueModel(fromSid, channel.getId(),configModel,brief);
    }


    public List<DialogueModel> getDialogueListBySubscriberId(Long sid, Integer pageNow) {
        List<Dialogue> dialogueList = dialogueRepository.findPageBySubscriberIdAndShowOuter(sid, ImConstant.YES,10 * (pageNow - 1),10);
        return dialogueList.stream().map(item -> new DialogueModel(item.getId(), item.getChannelId(), item.getType(), item.getName(), item.getAvatar(),
            item.getBrief(), item.getLastTime(), item.getUnreadCount())).collect(Collectors.toList());
    }

    public DialogueModel getDialogueByChannelId(Long sid, Long channelId) throws HandlerException{
        Dialogue dialogue = dialogueRepository.getBySubscriberIdAndChannelId(sid, channelId);
        HandlerExceptionHelper.throwExceptionIfNull(dialogue,"对话还未创建");
        return new DialogueModel(dialogue.getId(), channelId, dialogue.getType(), dialogue.getName(), dialogue.getAvatar(), dialogue.getBrief(), dialogue.getLastTime(), dialogue.getUnreadCount());
    }

    private ChannelConfigModel checkAndGetConfig(Long sid, Long channelId)throws HandlerException{
        Channel channel = channelRepository.findFirstById(channelId);
        HandlerExceptionHelper.throwExceptionIfNull(channel, "通讯频道不存在");
        ChannelConfigModel configModel = JSONObject.parseObject(channel.getChannelConfig(), ChannelConfigModel.class);
        List<Long> sidList = configModel.getSidList();
        HandlerExceptionHelper.throwException(!sidList.contains(sid), "非法请求!!!");
        configModel.setType(channel.getType());
        return configModel;
    }

    public List<SubscriberModel> getSubscriberModelListByChannelId(Long sid, Long channelId) throws HandlerException{
        ChannelConfigModel configModel = checkAndGetConfig(sid, channelId);
        List<Long> sidList = configModel.getSidList();
        List<Subscriber> list = subscriberRepository.findAllByIdIn(sidList);
        this.updateDialogAsync(sid,channelId,configModel,list);
        return list.stream().map(item -> new SubscriberModel(item.getId(), item.getNickname(), item.getAvatar(), item.getCreateTime(),item.getUniqueId(),item.getIntro())).collect(Collectors.toList());
    }

    // 异步修改会话资料
    private void updateDialogAsync(Long sid, Long channelId,ChannelConfigModel configModel,List<Subscriber> list){
        threadPoolTaskExecutor.execute(()->{
            Dialogue dialogue = dialogueRepository.getBySubscriberIdAndChannelId(sid, channelId);
            if (null != dialogue){
                Integer type = configModel.getType();
                if (type == ImConstant.CHANNEL_TYPE_PRIVATE && list.size() == 2){//私聊
                    Optional<Subscriber> first = list.stream().filter(s -> !s.getId().equals(sid)).findFirst();
                    if (first.isPresent()){
                        Subscriber ano = first.get();
                        if (!dialogue.getAvatar().equals(ano.getAvatar()) || !dialogue.getName().equals(ano.getNickname())){
                            dialogueRepository.updateAvatarAndName(dialogue.getId(),ano.getAvatar(),ano.getNickname());
                        }
                    }
                }else if (type == ImConstant.CHANNEL_TYPE_GROUP && list.size() > 2){//群聊
                    if (!dialogue.getAvatar().equals(configModel.getGroupAvatar()) || !dialogue.getName().equals(configModel.getGroupName())){
                        dialogueRepository.updateAvatarAndName(dialogue.getId(),configModel.getGroupAvatar(),configModel.getGroupName());
                    }
                }
            }
        });
    }

    @JuncoTransactional(rollbackFor = Exception.class)
    public MessageContentResponse sendMsgToChannel(MessageContentRequest request) throws HandlerException {
        // 已读确认与发送共用频道锁；未读计数和消息记录必须在同一事务提交。
        Channel lockedChannel = channelRepository.lockById(request.getChannelId());
        HandlerExceptionHelper.throwExceptionIfNull(lockedChannel, "通讯频道不存在");
        long now = System.currentTimeMillis();
        Long sid = request.getSenderId();
        Long channelId = request.getChannelId();
        ChannelConfigModel configModel = checkAndGetConfig(sid, channelId);
        List<Long> sidList = configModel.getSidList();
        List<Long> receiveIdList = new ArrayList<>();

        //判断消息摘要
        String brief;
        MessageContentModel contentModel = request.getContentModel();
        String ct = contentModel.getType();
        switch (ct) {
            case ImConstant.MESSAGE_CONTENT_TYPE_TEXT:
                HandlerExceptionHelper.throwException(MyStringUtil.isEmpty(contentModel.getText()),"文本内容不能为空");
                brief = MyStringUtil.ellipsis(contentModel.getText());
                break;
            case ImConstant.MESSAGE_CONTENT_TYPE_IMAGE:
                HandlerExceptionHelper.throwException(MyStringUtil.isEmpty(contentModel.getImageUrl()),"图片链接不能为空");
                brief = "[图片]";
                break;
            default:
                throw new HandlerException("不支持的消息类型");
        }
        Long fromSid = request.getSenderId();
        //处理对话列表
        int channelType = configModel.getType();
        switch (channelType) {
            case ImConstant.CHANNEL_TYPE_PRIVATE://私聊
                receiveIdList.add(fromSid);
                Optional<Long> aLong = sidList.stream().filter(id -> !fromSid.equals(id)).findFirst();
                if (!aLong.isPresent()) {
                    throw new HandlerException("私聊对象不存在");
                }
                Long toSid = aLong.get();
                receiveIdList.add(toSid);

                Dialogue fromDialogue = getPrivateDialogue(fromSid, toSid, request.getChannelId());
                dialogueRepository.updateHasRead(fromDialogue.getId(),now,brief);

                Dialogue toDialogue = getPrivateDialogue(toSid, fromSid, request.getChannelId());
                dialogueRepository.updateUnread(toDialogue.getId(),now,brief);
                break;
            case ImConstant.CHANNEL_TYPE_GROUP:
                receiveIdList.add(fromSid);
                DialogueModel groupFrom = getGroupDialogueModel(fromSid, channelId,configModel,brief);
                dialogueRepository.updateHasRead(groupFrom.getId(),now,brief);
                sidList.stream().filter(id -> !fromSid.equals(id)).forEach(id -> {
                    DialogueModel groupTo = getGroupDialogueModel(id, channelId,configModel,brief);
                    dialogueRepository.updateUnread(groupTo.getId(),now,brief);
                    receiveIdList.add(id);
                });
                break;

            default:
                throw new HandlerException("暂时不支持的聊天类型");
        }

        //消息入数据库
        ChannelMsg channelMsg = new ChannelMsg();
        //FIXME 高并发时可能会冲突
        channelMsg.setId(String.valueOf(LongUtil.getInstance().nextId()));
        channelMsg.setSubscriberId(request.getSenderId());
        channelMsg.setChannelId(request.getChannelId());
        channelMsg.setTime(now);
        channelMsg.setContent(FastJsonUtil.toCompatibleJSON(request.getContentModel()));
        channelMsgRepository.save(channelMsg);

        //更新频道的消息数
        channelRepository.updateMsgCount(request.getChannelId(),now);
        //组装返回模型
        MessageContentResponse ret = new MessageContentResponse();
        ret.setId(channelMsg.getId());
        ret.setChannelId(request.getChannelId());
        ret.setSenderId(request.getSenderId());
        ret.setContent(request.getContentModel());
        ret.setTime(now);
        ret.setReceiveIdList(receiveIdList);
        return ret;
    }

    /** 只确认已展示且仍是最新的消息。旧确认不清除后续消息的未读数，重复确认无副作用。 */
    @JuncoTransactional(rollbackFor = Exception.class)
    public DialogueModel markRead(Long sid, Long channelId, String messageId) throws HandlerException {
        Channel channel = channelRepository.lockById(channelId);
        HandlerExceptionHelper.throwExceptionIfNull(channel, "通讯频道不存在");
        checkAndGetConfig(sid, channelId);
        Dialogue dialogue = dialogueRepository.getBySubscriberIdAndChannelId(sid, channelId);
        HandlerExceptionHelper.throwExceptionIfNull(dialogue, "对话还未创建");
        ChannelMsg seen = channelMsgRepository.findFirstById(messageId);
        HandlerExceptionHelper.throwExceptionIfNull(seen, "消息不存在");
        HandlerExceptionHelper.throwException(!channelId.equals(seen.getChannelId()), "消息不属于当前会话");
        List<ChannelMsg> latest = channelMsgRepository.findPageByChannelId(channelId, 0, 1);
        int unread = dialogue.getUnreadCount();
        // 频道锁阻止发送线程在校验与清零之间插入新消息。
        if (!latest.isEmpty() && messageId.equals(latest.get(0).getId())) {
            dialogueRepository.markRead(dialogue.getId(), seen.getTime());
            unread = 0;
        }
        return new DialogueModel(dialogue.getId(), channelId, dialogue.getType(), dialogue.getName(),
                dialogue.getAvatar(), dialogue.getBrief(), dialogue.getLastTime(), unread);
    }

    public List<ChannelMsgModel> getMsgListByChannelId(Long sid, Long channelId, Integer pageNow) throws HandlerException {
//        checkAndGetConfig(sid, channelId);//不浪费性能再校验了
        List<ChannelMsg> msgList = channelMsgRepository.findPageByChannelId(channelId,10 * (pageNow - 1),10);
        return msgList.stream().map(item -> {
            ChannelMsgModel m = new ChannelMsgModel();
            m.setId(item.getId());
            m.setSenderId(item.getSubscriberId());
            m.setChannelId(channelId);
            m.setContent(JSONObject.parseObject(item.getContent(), MessageContentModel.class));
            m.setTime(item.getTime());
            return m;
        }).collect(Collectors.toList());
    }

}

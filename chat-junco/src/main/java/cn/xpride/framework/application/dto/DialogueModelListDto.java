package cn.xpride.framework.application.dto;

import cn.xpride.framework.application.model.DialogueModel;
import cn.xpride.framework.base.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DialogueModelListDto extends BaseResponse {

    /** 通讯对话列表 */
    private List<DialogueModel> dialogueList;

}

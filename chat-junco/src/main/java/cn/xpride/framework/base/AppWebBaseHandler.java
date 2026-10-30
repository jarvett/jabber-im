package cn.xpride.framework.base;

import cn.xpride.framework.application.model.MessageContentModel;
import cn.xpride.framework.utility.MyStringUtil;
import io.github.jarvett.web.JuncoHttpRequest;
import java.util.ArrayList;
import java.util.List;

/** 适配 Spring RequestParam 的查询、表单、重复参数与 token 优先级。 */
public abstract class AppWebBaseHandler {
    protected static String parameter(JuncoHttpRequest request, String name) {
        String value = request.getQueryParameter(name);
        return value != null ? value : request.getFormParameter(name);
    }

    protected static String required(JuncoHttpRequest request, String name) throws HandlerException {
        String value = parameter(request, name);
        if (value == null) {
            throw new HandlerException(400, "参数缺失:" + name + " 类型:String");
        }
        return value;
    }

    protected static Long longParam(JuncoHttpRequest request, String name) throws HandlerException {
        String value = parameter(request, name);
        if (value == null || value.isEmpty()) {
            throw new HandlerException(400, "参数缺失:" + name + " 类型:Long");
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            throw new HandlerException(400, "参数类型错误:" + name + " 需要类型:Long");
        }
    }

    protected static Integer page(JuncoHttpRequest request, String name) throws HandlerException {
        String value = parameter(request, name);
        if (value == null || value.isEmpty()) {
            return 1;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            throw new HandlerException(400, "参数类型错误:" + name + " 需要类型:Integer");
        }
    }

    protected static List<Long> longList(JuncoHttpRequest request, String name) throws HandlerException {
        String value = parameter(request, name);
        if (value == null) {
            throw new HandlerException(400, "参数缺失:" + name + " 类型:List");
        }
        List<Long> ids = new ArrayList<>();
        try {
            if (!value.isEmpty()) {
                for (String id : value.split(",", -1)) {
                    ids.add(id.isBlank() ? null : Long.valueOf(id.trim()));
                }
            }
            return ids;
        } catch (NumberFormatException e) {
            throw new HandlerException(400, "参数类型错误:" + name + " 需要类型:List");
        }
    }

    protected static MessageContentModel content(JuncoHttpRequest request) throws HandlerException {
        MessageContentModel model = new MessageContentModel();
        model.setType(parameter(request, "type"));
        model.setText(parameter(request, "text"));
        model.setImageUrl(parameter(request, "imageUrl"));
        model.setEventKey(parameter(request, "eventKey"));
        if (model.getType() == null || model.getType().isBlank()) {
            throw new HandlerException(400, "消息体类型不能为空");
        }
        return model;
    }

    protected MyToken getMyToken(JuncoHttpRequest request) throws HandlerException {
        String token = request.getHeader("token");
        if (MyStringUtil.isEmpty(token)) {
            token = parameter(request, "token");
        }
        if (MyStringUtil.isEmpty(token) && request.getHeader("Cookie") != null) {
            for (String cookie : request.getHeader("Cookie").split(";")) {
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2 && parts[0].equals("token")) {
                    token = parts[1];
                }
            }
        }
        MyToken model = MyTokenUtil.getToken(token);
        HandlerExceptionHelper.throwTokenExpiredIfNull(model);
        return model;
    }
}

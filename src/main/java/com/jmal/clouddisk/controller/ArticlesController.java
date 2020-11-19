package com.jmal.clouddisk.controller;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.PageUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.db.PageResult;
import com.jmal.clouddisk.model.MarkdownVO;
import com.jmal.clouddisk.model.Page;
import com.jmal.clouddisk.model.UserSettingDTO;
import com.jmal.clouddisk.service.IFileService;
import com.jmal.clouddisk.service.impl.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jmal
 * @Description 文章页面
 * @Date 2020/11/16 5:41 下午
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@Controller
public class ArticlesController {

    @Autowired
    private SettingService settingService;

    @Autowired
    private IFileService fileService;

    @GetMapping("/articles")
    public String articles(HttpServletRequest request, ModelMap map){
        int page = 1, pageSize = 10;
        String pIndex = request.getParameter("page");
        if(!StringUtils.isEmpty(pIndex)){
            page = Integer.parseInt(pIndex);
        }
        UserSettingDTO userSettingDTO = settingService.getWebsiteSetting();
        setOperatingButtonList(userSettingDTO);
        map.addAttribute("setting", userSettingDTO);
        map.addAttribute("articlesData", fileService.getArticles(page, pageSize));
        return "index";
    }

    @GetMapping("/articles/{slug}")
    public String index(HttpServletRequest request, @PathVariable String slug, ModelMap map){
        UserSettingDTO userSettingDTO = settingService.getWebsiteSetting();
        setOperatingButtonList(userSettingDTO);
        map.addAttribute("setting", userSettingDTO);
        map.addAttribute("markdown", fileService.getMarkDownContentBySlug(slug));
        return "article";
    }

    private void setOperatingButtonList(UserSettingDTO userSettingDTO) {
        if(userSettingDTO != null && !StringUtils.isEmpty(userSettingDTO.getOperatingButtons())){
            String operatingButtons = userSettingDTO.getOperatingButtons();
            List<UserSettingDTO.OperatingButton> operatingButtonList = new ArrayList<>();
            for (String button : operatingButtons.split("[\\n]")) {
                UserSettingDTO.OperatingButton operatingButton = new UserSettingDTO.OperatingButton();
                int splitIndex = button.indexOf(":");
                String label = button.substring(0, splitIndex);
                String title = ReUtil.getGroup0("[^><]+(?=<\\/i>)", label);
                if(StringUtils.isEmpty(title)){
                    title = "";
                }
                operatingButton.setTitle(title);
                operatingButton.setStyle(ReUtil.getGroup0("[^=\"<]+(?=\">)", label));
                operatingButton.setUrl(button.substring(splitIndex + 1));
                operatingButtonList.add(operatingButton);
            }
            userSettingDTO.setOperatingButtonList(operatingButtonList);
        }
    }

}



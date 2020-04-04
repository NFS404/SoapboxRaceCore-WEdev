package com.soapboxrace.core.bo;

import com.soapboxrace.core.vo.ModInfoVO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class ModdingBO {

    @EJB
    private ParameterBO parameterBO;

    // SBRW ModNet
    public ModInfoVO getModInfo() {
        if (!parameterBO.getBoolParam("MODDING_ENABLED")) {
            return null;
        }

        String moddingBasePath = parameterBO.getStrParam("MODDING_BASE_PATH");
        ModInfoVO modInfoVO = new ModInfoVO();
        modInfoVO.setServerID(parameterBO.getStrParam("MODDING_SERVER_ID"));
        modInfoVO.setBasePath(moddingBasePath == null ? "" : moddingBasePath);

        List<String> features = new ArrayList<>();

        String moddingFeatures = parameterBO.getStrParam("MODDING_FEATURES");

        if (moddingFeatures != null && !moddingFeatures.isEmpty()) {
            features.addAll(Arrays.asList(moddingFeatures.split(";")));
        }
        modInfoVO.setFeatures(features);

        return modInfoVO;
    }
}
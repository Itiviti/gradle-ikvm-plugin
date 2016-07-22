package com.ullink

import junit.framework.Assert;
import org.junit.Test;

public class IkvmBasePluginTest {

    @Test
    public void tryToFindUrlDefault() throws Exception {
        IkvmBasePlugin ikvmBasePlugin = new IkvmBasePlugin();
        String foundUrl = ikvmBasePlugin.tryToFindUrl("7.2.4630.5")
        Assert.assertTrue(foundUrl.contains("sourceforge"))
    }

    @Test
    public void tryToFindUrlAlternative() throws Exception {
        IkvmBasePlugin ikvmBasePlugin = new IkvmBasePlugin()
        String foundUrl = ikvmBasePlugin.tryToFindUrl("8.1.5717.0")
        Assert.assertTrue(foundUrl.contains("frijters"))
    }
}
package net.pedroricardo;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;

@Modmenu(modId = "paintingselector")
@Config(name = "painting-selector", wrapperName = "PSConfig")
public class PSConfigModel {
    public boolean fixSetPaintingPlacement = true;
    public boolean searchBar = true;
    public boolean narrateAll = false;
}

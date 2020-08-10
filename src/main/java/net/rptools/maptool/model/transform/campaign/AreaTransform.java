package net.rptools.maptool.model.transform.campaign;

import com.thoughtworks.xstream.XStream;
import net.rptools.lib.FileUtil;
import net.rptools.lib.ModelVersionTransformation;
import net.rptools.lib.geom.Area;
import java.util.regex.Pattern;

public class AreaTransform implements ModelVersionTransformation {

    private static final XStream xstream = FileUtil.getConfiguredXStream();
    private static final Pattern pattern = Pattern.compile("<curves>.+?</curves>", Pattern.DOTALL | Pattern.MULTILINE);

    @Override
    public String transform(String text) {
        String transformed = text;
        var matcher = pattern.matcher(transformed);
        while (matcher.find()) {
            var data = matcher.group();
            var xml = "<java.awt.geom.Area>" + data + "</java.awt.geom.Area>";
            var area = (java.awt.geom.Area)xstream.fromXML(xml);
            transformed = transformed.replace(data, xstream.toXML(new Area(area)));
        }
        return transformed.replaceAll("</?" + Area.class.getCanonicalName() + ">", "");
    }
}

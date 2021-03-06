package open.dolphin.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import open.dolphin.client.ClientContext;
import org.apache.velocity.Template;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;

/**
 * Templateというものをつかってみる
 * 
 * @author masuda, Masuda Naika
 */
public class TemplateLoader {
    
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    public Template newTemplate(String templateName) {

        RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();

        try (InputStream instream = ClientContext.getTemplateAsStream(templateName);
                InputStreamReader reader = new InputStreamReader(instream, ENCODING);) {

            SimpleNode node = runtimeServices.parse(reader, templateName);
            Template template = new Template();
            template.setRuntimeServices(runtimeServices);
            template.setData(node);
            template.initDocument();
            return template;

        } catch (IOException | ParseException ex) {
        }

        return null;
    }
}

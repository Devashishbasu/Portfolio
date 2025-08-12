package Devashish.Basu.Portfolio;




import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class StaticSiteExporter {

  public static void main(String[] args) throws IOException {
    // Start Spring without web server
    ApplicationContext ctx = new SpringApplicationBuilder(PortfolioApplication.class)
        .web(WebApplicationType.NONE)
        .run();

    // Thymeleaf configuration
    SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
    resolver.setApplicationContext(ctx);
    resolver.setPrefix("classpath:/templates/");
    resolver.setSuffix(".html");
    resolver.setTemplateMode("HTML");
    resolver.setCharacterEncoding("UTF-8");

    TemplateEngine engine = new SpringTemplateEngine();
    engine.setTemplateResolver(resolver);

    // Pages to export
    List<String> pages = List.of("home");

    // Output folder
    Path outputDir = Path.of("target/export");
    Files.createDirectories(outputDir);

    for (String page : pages) {
      Context context = new Context();
      String html = engine.process(page, context);

      File outFile = outputDir.resolve(page + ".html").toFile();
      try (FileWriter writer = new FileWriter(outFile)) {
        writer.write(html);
      }
      System.out.println("Generated: " + outFile.getAbsolutePath());
    }

    // Copy static assets
    Path staticDir = Path.of("src/main/resources/static");
    if (Files.exists(staticDir)) {
      Files.walk(staticDir).forEach(src -> {
        try {
          Path dest = outputDir.resolve(staticDir.relativize(src).toString());
          if (Files.isDirectory(src)) {
            Files.createDirectories(dest);
          } else {
            Files.copy(src, dest);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    }

    System.out.println("Static site export complete!");
  }
}

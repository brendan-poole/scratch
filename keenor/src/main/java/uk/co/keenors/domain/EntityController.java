package uk.co.keenors.domain;

import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/entitys")
@Controller
@RooWebScaffold(path = "entitys", formBackingObject = Entity.class)
public class EntityController {
}

package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class OptionsAmbientOcclusionFix extends DataFix {
    public OptionsAmbientOcclusionFix(Schema p_263497_) {
        super(p_263497_, false);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "OptionsAmbientOcclusionFix",
            this.getInputSchema().getType(References.OPTIONS),
            p_263493_ -> p_263493_.update(
                    DSL.remainderFinder(),
                    p_337653_ -> DataFixUtils.orElse(
                            p_337653_.get("ao").asString().map(p_263546_ -> p_337653_.set("ao", p_337653_.createString(updateValue(p_263546_)))).result(),
                            p_337653_
                        )
                )
        );
    }

    private static String updateValue(String p_263541_) {
        return switch (p_263541_) {
            case "0" -> "false";
            case "1", "2" -> "true";
            default -> p_263541_;
        };
    }
}

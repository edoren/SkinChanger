var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var OPCODES = Java.type('org.objectweb.asm.Opcodes');

var methodsData = {
    "PlayerInfo/getSkin": {
        "aliases": ["getSkin", "m_293823_"],
        "desc": "()Lnet/minecraft/client/resources/PlayerSkin;"
    },
    "SkullBlockRenderer/getRenderType": {
        "aliases": ["getRenderType", "m_112523_"],
        "desc": "(Lnet/minecraft/world/level/block/SkullBlock$Type;Lnet/minecraft/world/item/component/ResolvableProfile;)Lnet/minecraft/client/renderer/RenderType;"
    },
    "RenderType/entityTranslucent": {
        "aliases": ["entityTranslucent", "m_110473_"]
    },
    "RenderType/entityCutoutNoCullZOffset": {
        "aliases": ["entityCutoutNoCullZOffset"],
    },
    "PlayerTabOverlayGui/render": {
        "aliases": ["render", "m_280406_"],
        "desc": "(Lcom/mojang/blaze3d/vertex/PoseStack;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V"
    }
};

function checkMethod(key, method) {
    if (typeof method.name === 'undefined' || typeof method.desc === 'undefined') {
        return false;
    }

    var methodAliases = methodsData[key]["aliases"];
    var methodDesc = methodsData[key]["desc"];

    for (var i = 0; i < methodAliases.length; i++) {
        if (methodAliases[i] === method.name) {
            if (typeof methodDesc === 'undefined') {
                return true;
            } else if (method.desc === methodDesc) {
                return true;
            } else {
                return false;
            }
        }
    }

    return false;
}

// net/minecraft/client/multiplayer/PlayerInfo/getSkin
function transformMethod001(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (insn.getOpcode() === OPCODES.ARETURN) {
            var tmp = ASMAPI.getMethodNode();
            tmp.visitVarInsn(OPCODES.ASTORE, 2);
            tmp.visitVarInsn(OPCODES.ALOAD, 0);
            tmp.visitVarInsn(OPCODES.ALOAD, 2);
            tmp.visitMethodInsn(OPCODES.INVOKESTATIC,
                'me/edoren/skin_changer/client/ClientASMHooks',
                'getSkin',
                '(Lnet/minecraft/client/multiplayer/PlayerInfo;Lnet/minecraft/client/resources/PlayerSkin;)Lnet/minecraft/client/resources/PlayerSkin;',
                false);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}

// net/minecraft/client/renderer/blockentity/SkullBlockRenderer/getRenderType
function transformMethod002(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (OPCODES.INVOKESTATIC === insn.getOpcode() &&
            (checkMethod('RenderType/entityTranslucent', insn) || checkMethod('RenderType/entityCutoutNoCullZOffset', insn))) {
            var tmp = ASMAPI.getMethodNode();
            tmp.visitVarInsn(OPCODES.ASTORE, 2);
            tmp.visitVarInsn(OPCODES.ALOAD, 0);
            tmp.visitVarInsn(OPCODES.ALOAD, 1);
            tmp.visitVarInsn(OPCODES.ALOAD, 2);
            tmp.visitMethodInsn(OPCODES.INVOKESTATIC,
                'me/edoren/skin_changer/client/ClientASMHooks',
                'getRenderTypeSkull',
                '(Lnet/minecraft/world/level/block/SkullBlock$Type;Lnet/minecraft/world/item/component/ResolvableProfile;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceLocation;',
                false);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}

// net/minecraft/client/gui/components/PlayerTabOverlay/render
function transformMethod003(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (insn.getOpcode() === OPCODES.ISTORE && insn.var === 12) {
            // Set this to 1:
            // boolean flag = this.minecraft.isLocalServer() || this.minecraft.getConnection().getConnection().isEncrypted();
            var tmp = ASMAPI.getMethodNode();
            tmp.visitInsn(OPCODES.POP);
            tmp.visitInsn(OPCODES.ICONST_1);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}

function initializeCoreMod() {
    return {
        'Transformer001': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/multiplayer/PlayerInfo'
            },
            'transformer': function (node) {
                node.methods.forEach(function (method) {
//                    print("[SkinChanger Debug] PlayerInfo ", method.name);
                    if (checkMethod('PlayerInfo/getSkin', method)) {
                        print("[SkinChanger] Fixing method", method.name, "with signature", method.desc);
                        transformMethod001(method);
                    }
                });
                return node;
            }
        },
        'Transformer002': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/renderer/blockentity/SkullBlockRenderer'
            },
            'transformer': function (node) {
                node.methods.forEach(function (method) {
//                    print("[SkinChanger Debug] SkullBlockRenderer ", method.name);
                    if (checkMethod('SkullBlockRenderer/getRenderType', method)) {
                        print("[SkinChanger] Fixing method", method.name, "with signature", method.desc);
                        transformMethod002(method);
                    }
                });
                return node;
            }
        },
        'Transformer003': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/gui/components/PlayerTabOverlay'
            },
            'transformer': function (node) {
                node.methods.forEach(function (method) {
//                    print("[SkinChanger Debug] PlayerTabOverlay ", method.name);
                    if (checkMethod('PlayerTabOverlayGui/render', method)) {
                        print("[SkinChanger] Fixing method", method.name, "with signature", method.desc);
                        transformMethod003(method);
                    }
                });
                return node;
            }
        }
    };
}

var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var OPCODES = Java.type('org.objectweb.asm.Opcodes');

var methodsData = {
    "PlayerInfo/getSkinLocation": {
        "aliases": ["getSkinLocation", "m_105337_"],
        "desc": "()Lnet/minecraft/resources/ResourceLocation;"
    },
    "PlayerInfo/getCapeLocation": {
        "aliases": ["getCapeLocation", "m_105338_"],
        "desc": "()Lnet/minecraft/resources/ResourceLocation;"
    },
    "PlayerInfo/getModelName": {
        "aliases": ["getModelName", "m_105336_"],
        "desc": "()Ljava/lang/String;"
    },
    "SkullBlockRenderer/getRenderType": {
        "aliases": ["getRenderType", "m_112523_"],
        "desc": "(Lnet/minecraft/world/level/block/SkullBlock$Type;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/client/renderer/RenderType;"
    },
    "RenderType/entityTranslucent": {
        "aliases": ["entityTranslucent", "m_110473_"]
    },
    "RenderType/entityCutoutNoCull": {
        "aliases": ["entityCutoutNoCull", "m_110458_"],
    },
    "PlayerTabOverlayGui/render": {
        "aliases": ["render", "m_94544_"],
        "desc": "(Lcom/mojang/blaze3d/vertex/PoseStack;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V"
    },
    "TextureManager/setShaderTexture": {
        "aliases": ["setShaderTexture", "m_157456_"],
        "desc": "(Lnet/minecraft/resources/ResourceLocation;)V"
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

// net/minecraft/client/multiplayer/PlayerInfo/getSkinLocation
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
                'getSkinTextureLocation',
                '(Lnet/minecraft/client/multiplayer/PlayerInfo;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceLocation;',
                false);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}

// net/minecraft/client/multiplayer/PlayerInfo/getCapeLocation
function transformMethod002(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (insn.getOpcode() === OPCODES.ARETURN) {
            var tmp = ASMAPI.getMethodNode();
            tmp.visitVarInsn(OPCODES.ASTORE, 2);
            tmp.visitVarInsn(OPCODES.ALOAD, 0);
            tmp.visitVarInsn(OPCODES.ALOAD, 2);
            tmp.visitMethodInsn(OPCODES.INVOKESTATIC,
                'me/edoren/skin_changer/client/ClientASMHooks',
                'getCloakTextureLocation',
                '(Lnet/minecraft/client/multiplayer/PlayerInfo;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceLocation;',
                false);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}

// net/minecraft/client/multiplayer/PlayerInfo/getModelName
function transformMethod003(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (insn.getOpcode() === OPCODES.ARETURN) {
            var tmp = ASMAPI.getMethodNode();
            tmp.visitVarInsn(OPCODES.ASTORE, 2);
            tmp.visitVarInsn(OPCODES.ALOAD, 0);
            tmp.visitVarInsn(OPCODES.ALOAD, 2);
            tmp.visitMethodInsn(OPCODES.INVOKESTATIC,
                'me/edoren/skin_changer/client/ClientASMHooks',
                'getModelName',
                '(Lnet/minecraft/client/multiplayer/PlayerInfo;Ljava/lang/String;)Ljava/lang/String;',
                false);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}

// net/minecraft/client/renderer/blockentity/SkullBlockRenderer/getRenderType
function transformMethod004(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (OPCODES.INVOKESTATIC === insn.getOpcode() &&
            (checkMethod('RenderType/entityTranslucent', insn) || checkMethod('RenderType/entityCutoutNoCull', insn))) {
            var tmp = ASMAPI.getMethodNode();
            tmp.visitVarInsn(OPCODES.ASTORE, 2);
            tmp.visitVarInsn(OPCODES.ALOAD, 0);
            tmp.visitVarInsn(OPCODES.ALOAD, 1);
            tmp.visitVarInsn(OPCODES.ALOAD, 2);
            tmp.visitMethodInsn(OPCODES.INVOKESTATIC,
                'me/edoren/skin_changer/client/ClientASMHooks',
                'getRenderTypeSkull',
                '(Lnet/minecraft/world/level/block/SkullBlock$Type;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceLocation;',
                false);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}

// net/minecraft/client/gui/components/PlayerTabOverlay/render
function transformMethod005(node) {
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
                    if (checkMethod('PlayerInfo/getSkinLocation', method)) {
                        print("[SkinChanger] Fixing method", method.name, "with signature", method.desc);
                        transformMethod001(method);
                    } else if (checkMethod('PlayerInfo/getCapeLocation', method)) {
                        print("[SkinChanger] Fixing method", method.name, "with signature", method.desc);
                        transformMethod002(method);
                    } else if (checkMethod('PlayerInfo/getModelName', method)) {
                        print("[SkinChanger] Fixing method", method.name, "with signature", method.desc);
                        transformMethod003(method);
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
                    if (checkMethod('SkullBlockRenderer/getRenderType', method)) {
                        print("[SkinChanger] Fixing method", method.name, "with signature", method.desc);
                        transformMethod004(method);
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
                    if (checkMethod('PlayerTabOverlayGui/render', method)) {
                        print("[SkinChanger] Fixing method", method.name, "with signature", method.desc);
                        transformMethod005(method);
                    }
                });
                return node;
            }
        }
    };
}

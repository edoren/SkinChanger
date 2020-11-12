var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var OPCODES = Java.type('org.objectweb.asm.Opcodes');

var methodsData = {
    "AbstractClientPlayerEntity/getLocationCape": {
        "aliases": ["getLocationCape", "func_110303_q"],
        "desc": "()Lnet/minecraft/util/ResourceLocation;"
    },
    "AbstractClientPlayerEntity/getLocationSkin": {
        "aliases": ["getLocationSkin", "func_110306_p"],
        "desc": "()Lnet/minecraft/util/ResourceLocation;"
    },
    "AbstractClientPlayerEntity/getSkinType": {
        "aliases": ["getSkinType", "func_175154_l"],
        "desc": "()Ljava/lang/String;"
    },
    "SkullTileEntityRenderer/getRenderType": {
        "aliases": ["getRenderType", "func_228878_a_"],
        "desc": "(Lnet/minecraft/block/SkullBlock$ISkullType;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/client/renderer/RenderType;"
    },
    "PlayerTabOverlayGui/render": {
        "aliases": ["render", "func_238523_a_"],
        "desc": "(Lcom/mojang/blaze3d/matrix/MatrixStack;ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreObjective;)V"
    },
    "TextureManager/bindTexture": {
        "aliases": ["bindTexture", "func_110577_a"],
        "desc": "(Lnet/minecraft/util/ResourceLocation;)V"
    }
};

function checkMethod(key, method) {
    if (typeof method.name === 'undefined' || typeof method.desc === 'undefined') {
        return false;
    }

    var methodAliases = methodsData[key]["aliases"];
    var methodDesc = methodsData[key]["desc"];

    if (method.desc === methodDesc) {
        for (var i = 0; i < methodAliases.length; i++) {
            if (methodAliases[i] === method.name) {
                print("[SkinChanger] Fixing method", key);
                return true;
            }
        }
    }

    return false;
}

// net/minecraft/client/entity/player/AbstractClientPlayerEntity/getLocationCape
function transformMethod001(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (insn.getOpcode() === OPCODES.ARETURN) {
            var tmp = ASMAPI.getMethodNode();
            tmp.visitVarInsn(OPCODES.ASTORE, 2);
            tmp.visitVarInsn(OPCODES.ALOAD, 0);
            tmp.visitVarInsn(OPCODES.ALOAD, 2);
            tmp.visitMethodInsn(OPCODES.INVOKESTATIC, 'me/edoren/skin_changer/client/ClientASMHooks', 'getLocationCape', '(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/util/ResourceLocation;', false);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}

// net/minecraft/client/entity/player/AbstractClientPlayerEntity/getLocationSkin
function transformMethod002(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (insn.getOpcode() === OPCODES.ARETURN) {
            var tmp = ASMAPI.getMethodNode();
            tmp.visitVarInsn(OPCODES.ASTORE, 2);
            tmp.visitVarInsn(OPCODES.ALOAD, 0);
            tmp.visitVarInsn(OPCODES.ALOAD, 2);
            tmp.visitMethodInsn(OPCODES.INVOKESTATIC, 'me/edoren/skin_changer/client/ClientASMHooks', 'getLocationSkin', '(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/util/ResourceLocation;', false);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}

// net/minecraft/client/entity/player/AbstractClientPlayerEntity/getSkinType
function transformMethod003(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (insn.getOpcode() === OPCODES.ARETURN) {
            var tmp = ASMAPI.getMethodNode();
            tmp.visitVarInsn(OPCODES.ASTORE, 2);
            tmp.visitVarInsn(OPCODES.ALOAD, 0);
            tmp.visitVarInsn(OPCODES.ALOAD, 2);
            tmp.visitMethodInsn(OPCODES.INVOKESTATIC, 'me/edoren/skin_changer/client/ClientASMHooks', 'getSkinType', '(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;Ljava/lang/String;)Ljava/lang/String;', false);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}

// net/minecraft/client/renderer/tileentity/SkullTileEntityRenderer/getRenderType
function transformMethod004(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (insn.getOpcode() === OPCODES.ARETURN) {
            var tmp = ASMAPI.getMethodNode();
            tmp.visitVarInsn(OPCODES.ASTORE, 2);
            tmp.visitVarInsn(OPCODES.ALOAD, 0);
            tmp.visitVarInsn(OPCODES.ALOAD, 1);
            tmp.visitVarInsn(OPCODES.ALOAD, 2);
            tmp.visitMethodInsn(OPCODES.INVOKESTATIC, 'me/edoren/skin_changer/client/ClientASMHooks', 'getRenderTypeSkull', '(Lnet/minecraft/block/SkullBlock$ISkullType;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/renderer/RenderType;)Lnet/minecraft/client/renderer/RenderType;', false);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}

// net/minecraft/client/gui/overlay/PlayerTabOverlayGui/render
function transformMethod005(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (insn.getOpcode() === OPCODES.ISTORE && insn.var === 12) {
            // Set this to 1:
            // boolean flag = this.mc.isIntegratedServerRunning() || this.mc.getConnection().getNetworkManager().isEncrypted();
            var tmp = ASMAPI.getMethodNode();
            tmp.visitInsn(OPCODES.POP);
            tmp.visitInsn(OPCODES.ICONST_1);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        } else if (insn.getOpcode() === OPCODES.INVOKEVIRTUAL && checkMethod('TextureManager/bindTexture', insn)) {
            var tmp = ASMAPI.getMethodNode();
            tmp.visitVarInsn(OPCODES.ASTORE, 33);
            tmp.visitVarInsn(OPCODES.ALOAD, 27);
            tmp.visitVarInsn(OPCODES.ALOAD, 33);
            tmp.visitMethodInsn(OPCODES.INVOKESTATIC, 'me/edoren/skin_changer/client/ClientASMHooks', 'getLocationTabOverlaySkin', '(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/util/ResourceLocation;', false);
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
                'name': 'net/minecraft/client/entity/player/AbstractClientPlayerEntity'
            },
            'transformer': function (node) {
                node.methods.forEach(function (method) {
                    if (checkMethod('AbstractClientPlayerEntity/getLocationCape', method)) {
                        transformMethod001(method);
                    } else if (checkMethod('AbstractClientPlayerEntity/getLocationSkin', method)) {
                        transformMethod002(method);
                    } else if (checkMethod('AbstractClientPlayerEntity/getSkinType', method)) {
                        transformMethod003(method);
                    }
                });
                return node;
            }
        },
        'Transformer002': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/renderer/tileentity/SkullTileEntityRenderer'
            },
            'transformer': function (node) {
                node.methods.forEach(function (method) {
                    if (checkMethod('SkullTileEntityRenderer/getRenderType', method)) {
                        transformMethod004(method);
                    }
                });
                return node;
            }
        },
        'Transformer003': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/gui/overlay/PlayerTabOverlayGui'
            },
            'transformer': function (node) {
                node.methods.forEach(function (method) {
                    if (checkMethod('PlayerTabOverlayGui/render', method)) {
                        transformMethod005(method);
                    }
                });
                return node;
            }
        }
    };
}

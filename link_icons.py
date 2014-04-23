import os
import shutil

mappings = {}

source = "oxygen/oxygen/"

resolutions = {
    "256x256": ""
}

for res in resolutions.keys():
    target = "res/drawable" + resolutions[res]
    source_res = os.path.join(source, res)

    for ic in os.listdir(os.path.join(source_res, "mimetypes/")):
        target_name = "mime_" + ic[:-4].replace('-', '_').replace('+', '_').replace('.', '') + ".png"
        mappings[os.path.join(target, target_name)] = os.path.join(source_res, "mimetypes/", ic)

    mappings[os.path.join(target, "blank.png")] = os.path.join(source_res, "mimetypes/unknown.png")
    mappings[os.path.join(target, "drive_hdd.png")] = os.path.join(source_res, "devices/drive-harddisk.png")
    mappings[os.path.join(target, "drive_sd.png")] = os.path.join(source_res, "devices/media-flash-sd-mmc.png")
    mappings[os.path.join(target, "drive_usb.png")] = os.path.join(source_res, "devices/drive-removable-media-usb.png")
    mappings[os.path.join(target, "folder.png")] = os.path.join(source_res, "places/folder.png")

    if os.path.exists(target):
        shutil.rmtree(target)
    os.mkdir(target)

mappings[os.path.join("res/drawable", "icon.png")] = os.path.join(source_res, "places/folder.png")

for to in mappings.keys():
    fr = mappings[to]
    print "Coyping " + fr + " to " + to
    shutil.copyfile(fr, to)
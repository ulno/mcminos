'''
Created on Apr 18, 2010

@author: ulno

Create from the images path the corresponding directory in images.
inkscape is needed for this

'''

import glob
import sys
import os
import re
import subprocess
import distutils.dir_util
import pygame

# the source images in SVG

IMAGE_DIRECTORY=os.path.join("..","images")

# the directory where the images will be saved
#OUTPUT_DIRECTORY=os.path.join("..","..","android","assets","entities")
# this will be the intermediate directory for serving as the input for the texture packer
OUTPUT_DIRECTORY=os.path.join("..","images-packer-input")

# the generated source-file to load and access the images
OUTPUT_FILE=os.path.join("..","..","core","src","com","mcminos","game","Entities.java")

IMAGE_FORMAT="RGBA"
CONFIG_FILE="CONFIG"
README_FILE="README"

# options, which will be evaluated from CONFIG and their defaults
config_options={
    "symbol":'\0', # the symbol representing the entity
    "size":(1,1), # the multiplier for the size (castle:2, explosion: 3)
    "speed":100, # the animation speed or animation speed list (10 frames per second default)
    "order":[0], # the order of the images
    "zindex":700, # show this on which level?
    "focus":(1,1), # anchor point of the icon
    "moving":False, # This object moves by itself (like mcminos or the ghosts)
    }

# w equals h, so only one value here, to speed up gaming, resolutions must be powers of 2
SIZE_LIST = [128,64,32,16]
# 8 and 4 are generated from mini
#SIZE_LIST = [128,64,16] # for testing
SIZE_LIST_MINI = [8,4]

#try:
#    os.rmdir(OUTPUT_DIRECTORY)
#except OSError:
#    pass # ignore missing directory
#os.mkdir(OUTPUT_DIRECTORY)

filename_format = re.compile("(?P<config>"+CONFIG_FILE+")|" + # can be config
                             "(?P<readme>"+README_FILE+")|" + # or readme
                             "(?P<minidesc>.*)" + # some descriptive stuff, which will be moved to the naming part
                             "(?P<mini>mini\.svg)|" + # or mini image
                             "(?:" + # or
                             "(?P<description>.*)" + # some descriptive stuff, which will be moved to the naming part
                             "(?P<animation_number>\d\d)" + # animation number
                             "(?:\.(?P<svg>svg)|-(?P<width>\d\d)x(?P<height>\d\d)\.png)" + # extension
                             ")$") 
#                             "(?P<name>[a-zA-Z]" + # Start with a letter
#                             "[a-zA-Z0-9_\-]*)" + # Continue with several alpha numerics or - or _
#                             "(?:-x(?P<scaling>\d\d))?" + # Have maybe scaling
invalid_name_chars = re.compile("[\-\.]")

config_dictionary={}
config_dictionary[""]=config_options
entity_list={} # The dictionary storing entities
images_count = 0 # total number of images

class Animation_Image():
    '''One image of an animation.'''
    def __init__(self, image, animation_step):
        self.image = image
        self.animation_step = animation_step
    def get(self):
        return (self.image, self.animation_step)
        
class Graphics_Element():
    def __init__(self, name, config):
        """
        config must have the form of the config_options
        """
        self.image_dictionary = {} # this dictionary includes dictionaries with sizes per category
        self.name = name
        self.config = config.copy()
        print "Creating new entity", name, " with config", config
    def add_image(self, 
                  png_file_name, 
                  block_resolution,
                  multiple_w,multiple_h,
                  animation_step = 0):
        if not block_resolution in self.image_dictionary:
            self.image_dictionary[block_resolution] = {} # This will store the images of the animations under their corresponding step-number
        self.image_dictionary[block_resolution][animation_step] = \
            Animation_Image(png_file_name, animation_step)
        print "Adding to graphic:",self.name,\
            "storage resolution:",block_resolution,\
            "real size:",(multiple_w*block_resolution,multiple_h*block_resolution),\
            "step:",animation_step
#    def check_size(self, width, category="default"):
#        if category in self.image_dictionary:
#            return width in self.image_dictionary[category]
    def code(self,preload):
        '''
        get some code for the current graphics element.
        if preload == 1 then just generate code for asset-manager
        '''
        global images_count

        first_size = self.image_dictionary.keys()[0]
        if preload == 0:
            anisteps = self.image_dictionary[first_size].keys()
            if self.config["order"] == None:
                self.config["order"] = anisteps
            if not self.config["speed"] is list:
                self.config["speed"] = [self.config["speed"]] \
                    * len(self.config["order"])
        
        current = "" + self.name
        if preload == 0:
            code = "%s = new Graphics(\'%s\',%s,%s,%s,%s,%s,%s);\n" % \
                ( current, self.config["symbol"], 
                self.config["focus"][0]-1, self.config["focus"][1]-1,
                self.config["zindex"], repr(self.config["moving"]).lower(),
                self.config["size"][0], self.config["size"][1])
        else: code = ""
        for size in self.image_dictionary:
                anisteps = self.image_dictionary[size].keys()
                anisteps.sort()
                step_nr = 0 # allways start with 0
                for anistep in anisteps:
                    (img, _) = self.image_dictionary[size][anistep].get()
                    image_name = "%s_%s" \
                        % (self.name,anistep)
                    #file_name = os.path.join( OUTPUT_DIRECTORY, image_name + ".png" )
                    #loc_name = os.path.join("entities", image_name + ".png")
                    loc_name = os.path.join(image_name)
                    if preload == 0:
                        code += "%s.addImage( atlas, \"%s\", %s, %s );\n" % ( current, \
                            loc_name, \
                            size, step_nr)
                        images_count += 1
                    else:
                        code += "manager.load( \"%s\", Texture.class );\n" % loc_name
                    step_nr += 1
        if preload == 0:
          for size in self.image_dictionary:
            step_nr = 0 # allways start with 0
            anisteps = self.image_dictionary[size].keys()
            assert len(self.config['speed']) == len(self.config['order']), \
                    "number of animation times does not match config."
            anisteps.sort()
            print self.config['speed'], self.config['order'], anisteps
            if len(self.config['order']) <= 1:
                self.config['order'] = anisteps[:]
            if len(self.config['speed']) == 1:
                self.config['speed'] = self.config['speed'] * len(anisteps)
            print self.config['speed'], self.config['order'], anisteps
            step_nr = 0                
            for anistep in self.config['order']:
                image_nr = anisteps.index( self.config["order"][step_nr] )
                code += "%s.addAnimationStep( %s, %s );\n" % \
                    (current, image_nr, \
                        self.config['speed'][step_nr])
                step_nr += 1
            break # only do for one size (should be the same for all sizes)
        return code

# main program -- walk input directory
def cleanup_description(desc):
    if description == "": return ""
    # cut non-numerical letters from left and right
    while(not desc[0].isalpha()):
        desc= desc[1:]
    while(not desc[-1].isalpha()):
        desc= desc[:-1]
    # transform all weird letters in between
    desc=re.sub(invalid_name_chars, "_", desc) # replace . and - with _
    return desc


def generate_name(name, description):
    if name == "":
        name = description
    else:
        if description != "":
            name = name + "_" + description
    return name


def convert_images(sizes, name, animation_number, root, file):
    # check if entity exists
    if not name in entity_list:
        entity_list[name] = Graphics_Element(name,config)
    current_entity = entity_list[name]
    scaling=config["size"]
    multiple_w=scaling[0]
    multiple_h=scaling[1]
    assert multiple_w>0 and multiple_h>0, "Wrong scaling."
    for resolution in sizes:
        # make sure destination directory exists
        distutils.dir_util.mkpath( os.path.join( OUTPUT_DIRECTORY, str(resolution) ) )
        packfile = os.path.join( OUTPUT_DIRECTORY, str(resolution), "pack.json" )
        if not os.path.isfile(packfile): # create if necessary
            res = max(1024,int(resolution)*16)
            pf=open(packfile,"w")
            pf.write("{\nuseIndexes: false,\nmaxWidth: %s,\nmaxHeight: %s\n}"%(res,res))
            pf.close()
        # convert file with inkscape
        #print "width",width,"mutiples",multiple_w,multiple_h,"total",int(width)*multiple_w,int(width)*multiple_h
        image_name = "%s_%s" \
                     % (name,animation_number)
        output_file = os.path.join( OUTPUT_DIRECTORY, str(resolution), image_name + ".png" ) # check if destination already exists and skip
        # only change if not exists or source is newer
        if not os.path.isfile(output_file) or os.path.getmtime(os.path.join(root,file)) > os.path.getmtime(output_file):
            p = subprocess.Popen(["inkscape",
                                  "-w", "%d"%(int(resolution)*multiple_w),
                                  "-h", "%d"%(int(resolution)*multiple_h),
                                  "-e", output_file,
                                  os.path.join(root,file)],
                                 stdout=subprocess.PIPE)
            p.wait()
        current_entity.add_image(output_file,
                                 int(resolution),
                                 multiple_w,multiple_h,
                                 animation_step = animation_number)


for root, dirs, files in os.walk(IMAGE_DIRECTORY,topdown=True):
    short_root = root[len(IMAGE_DIRECTORY)+1:] # cut off root
    if len(short_root) > 0: # not main directory - any sub-directory, only folders in main-directory are read
        # construct the name
        short_root_elements = short_root.split(os.path.sep)
        root_class_name = "_".join(short_root_elements)
        complete_name = re.sub(invalid_name_chars,"_",root_class_name)  # replace . and - with
        #  find parent
        parent_path = "_".join(short_root_elements[:-1])
        parent_name = re.sub(invalid_name_chars, "_", parent_path)  # replace . and - with _
        # as top-down, needs to exist
        #config_dictionary[complete_name] = config_dictionary[parent_name].copy()
        new_config = config_dictionary[parent_name].copy()
        print "Taking initial configuration from parent_path", parent_path, "Config:", new_config
        # We are in a new directory -> Parse CONFIG, if available
        if CONFIG_FILE in files:
            # make config active in current scope
            for option in new_config:
                exec( "%s=%s" % (option, repr(new_config[option]) ))
            # we execute any python code here, nothing for the enduser (TODO: maybe check format here)
            exec(open(os.path.join(root,CONFIG_FILE)).read())
            #new_config = {}
            # write back to dictionary
            for option in new_config:
                new_config[option]=eval(option)
            config_dictionary[complete_name]=new_config
            print "Parsed configuration in ", root, "for ",complete_name,":", new_config
        config_dictionary[complete_name] = new_config
        config = config_dictionary[complete_name]
        # check scaling parameter
        if not isinstance(config["size"],tuple): # probably two coordinates
            scaling=config["size"]
            config["size"]=(scaling,scaling)
        for file in files:
            name = complete_name # init base-name
            print "Working on: path:", short_root, "File name:", file
            m = filename_format.match(file) # analyze filename
            if m==None:
                print "wrong filename format"
            elif m.group("config") != None \
              or  m.group("readme") != None: # ignore config, readme
                print "ignored or parsed before"
                print
            elif m.group("mini") != None: # This is the mini file
                description = cleanup_description(m.group("minidesc"))
                name = generate_name(name, description)
                print "Found mini-file for", name ,"for configuration", config
                convert_images(SIZE_LIST_MINI,name,animation_number,root,file)
            else:  # seems to be a regular image
                description=m.group("description")
                animation_number=int(m.group("animation_number"))
                svg = m.group("svg") != None
                width=None
                height=None
                if not svg:
                    width = int(m.group("width"))
                    height = int(m.group("height"))
                # if there is a description add it to the name
                if description != "":
                    description = cleanup_description( description )
                    name = generate_name( name, description )
                # print report
                print \
"""name: %(name)s
animation-nr: %(animation_number)s
is-svg: %(svg)s
width: %(width)s
height: %(height)s
config: %(config)s
"""%locals()
                # if there is a name, convert images
                if name != "":
                    if svg: # if this is an svg, create all missing sizes
                        convert_images(SIZE_LIST,name,animation_number,root,file)
                    else: # This is already a pixel-graphics (no svg)
                        pass # TODO: overwrite or add this

####### all parsed, now print code
print "Writing output file %s." % OUTPUT_FILE
f=open(OUTPUT_FILE,"w")
f.write( \
"""package com.mcminos.game;

/* Attention: this file is auto-generated by convert_images, 
 * do not change. */
 
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;


/* class is a singleton */
class Entities {
    private static Entities ourInstance = new Entities();

    public static Entities getInstance() {
        return ourInstance;
    }
""" )

for e in entity_list:
    f.write( "public static Graphics %s;\n"%e )

resList = SIZE_LIST[:]
resList.sort(reverse=True)
f.write("\n    public final static int[] resolutionList={%s};\n" \
    % (",".join(map(str,resList))) )

f.write( \
"""
    public static void scheduleLoad(AssetManager manager) {
""" )
#for e in entity_list:
#    print "Working on", e
#    f.write( entity_list[e].code(1) )
f.write("manager.load(\"entities/pack.atlas\",TextureAtlas.class);")

f.write( \
"""
    } // end scheduleLoad
""" )

f.write( \
"""
    public static void finishLoad(TextureAtlas atlas) {
""" )
for e in entity_list:
    print "Working on", e
    f.write( entity_list[e].code(0) )
    f.write( "%s.finishInit();\n"%e )

f.write( \
"""
    } // end finishLoad
""" )

f.write( \
"""
    public static int numberImages = %d;
} // end Entities class
""" % images_count)
f.close()

# now call teh packer
print "============= Now packing textures ============"
os.chdir("..")
os.chdir("..")
os.system("./gradlew texturePacker --stacktrace")
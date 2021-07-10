package nl.knokko.test5;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkClearDepthStencilValue;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXTI;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkExtent3D;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageSubresourceLayers;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkMemoryType;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkOffset3D;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import nl.knokko.test.Performance;

import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.EXTDebugUtils.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;

import static nl.knokko.test.Performance.next;

public class TriangleTest {

    static boolean DEBUG = true;

    static final int UINT_MAX = -1;
    static final int MAX_FRAMES_IN_FLIGHT = 2;

    static final String[] REQUIRED_DEBUG_LAYERS = { "VK_LAYER_LUNARG_standard_validation" };

    static final String[] REQUIRED_DEVICE_BASE_EXTENSIONS = { "VK_KHR_swapchain" };

    static final String[] REQUIRED_DEVICE_DEBUG_EXTENSIONS = {};

    static final String[] REQUIRED_INSTANCE_BASE_EXTENSIONS = {};

    static final String[] REQUIRED_INSTANCE_DEBUG_EXTENSIONS = { "VK_EXT_debug_utils" };

    static final int WIDTH = 800;
    static final int HEIGHT = 600;

    public static void main(String[] args) {
        DEBUG = args.length > 0 && args[0].equals("debug");
        TriangleTest triTest = new TriangleTest();
        triTest.run();

        // TODO Done with mipmaps
    }

    long window;

    VkInstance instance;

    VkPhysicalDevice physicalDevice;
    VkDevice device;

    VkQueue graphicsQueue;
    VkQueue presentQueue;

    long debugMessenger;

    long surface;

    long swapchain;
    LongBuffer swapchainImages;
    int swapchainImageFormat;
    VkExtent2D swapchainImageExtent;
    LongBuffer swapchainImageViews;

    long renderPass;
    long descriptorSetLayout;
    long pipelineLayout;
    long graphicsPipeline;

    LongBuffer swapchainFrameBuffers;

    long commandPool;
    long descriptorPool;
    long[] descriptorSets;
    PointerBuffer commandBuffers;

    int currentFrame = 0;
    long[] imageAvailableSemaphores = new long[MAX_FRAMES_IN_FLIGHT];
    long[] renderFinishedSemaphores = new long[MAX_FRAMES_IN_FLIGHT];
    long[] inFlightFences = new long[MAX_FRAMES_IN_FLIGHT];

    boolean framebufferResized = false;

    long vertexBuffer;
    long vertexBufferMemory;
    long indexBuffer;
    long indexBufferMemory;

    LongBuffer uniformBuffers;
    LongBuffer uniformBuffersMemory;

    int mipLevels;

    long textureImage;
    long textureImageMemory;
    long textureImageView;
    long textureSampler;

    long depthImage;
    long depthImageMemory;
    long depthImageView;

    void run() {

        //Performance.disable();
        initWindow();
        initVulkan();
        mainLoop();
        cleanUp();
    }

    void initWindow() {
        next("glfwInit");
        GLFW.glfwInit();
        next("glfw window hints");
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
        next("glfw create window");
        window = GLFW.glfwCreateWindow(WIDTH, HEIGHT, "First Triangle", NULL, NULL);
        GLFW.glfwSetFramebufferSizeCallback(window, (long window, int newWidth, int newHeight) -> {
            framebufferResized = true;
        });
        Performance.end();
    }

    void initVulkan() {
        //next("create vulkan instance"); Disabled because this part is more detailed
        createInstance();
        next("setup debug messenger");
        setupDebugMessenger();
        next("create surface");
        createSurface();
        next("pick physical device");
        pickPhysicalDevice();
        next("create logical device");
        createLogicalDevice();
        next("create swapchain");
        createSwapchain();
        next("create image views");
        createImageViews();
        next("create render pass");
        createRenderPass();
        next("create descriptor set layout");
        createDescriptorSetLayout();
        next("create graphics pipeline");
        createGraphicsPipeline();
        next("create command pool");
        createCommandPool();
        next("create depth resources");
        createDepthResources();
        next("create framebuffers");
        createFramebuffers();
        next("create texture image");
        createTextureImage();
        next("create texture image view");
        createTextureImageView();
        next("create texture sampler");
        createTextureSampler();
        next("create vertex buffers");
        createVertexBuffers();
        next("create index buffers");
        createIndexBuffers();
        next("create uniform buffers");
        createUniformBuffers();
        next("create descriptor pool");
        createDescriptorPool();
        next("create descriptor sets");
        createDescriptorSets();
        next("create command buffers");
        createCommandBuffers();
        next("create semaphores");
        createSyncObjects();
        Performance.end();
    }

    boolean checkValidationLayerSupport() {
        int[] availableLayerCountArray = { -1 };
        validate(VK10.vkEnumerateInstanceLayerProperties(availableLayerCountArray, null));
        int availableLayerCount = availableLayerCountArray[0];
        VkLayerProperties.Buffer availableLayers = VkLayerProperties.create(availableLayerCount);
        validate(VK10.vkEnumerateInstanceLayerProperties(availableLayerCountArray, availableLayers));

        System.out.println("Avilable validations layers:");
        for (int availableIndex = 0; availableIndex < availableLayerCount; availableIndex++) {
            VkLayerProperties layer = availableLayers.get(availableIndex);
            String availableName = layer.layerNameString();
            System.out.println(availableName);
        }
        System.out.println();
        System.out.println();

        requiredLoop: for (String requiredLayer : REQUIRED_DEBUG_LAYERS) {
            for (int availableIndex = 0; availableIndex < availableLayerCount; availableIndex++) {
                VkLayerProperties layer = availableLayers.get(availableIndex);
                String availableName = layer.layerNameString();
                if (availableName.equals(requiredLayer)) {
                    continue requiredLoop;
                }
            }
            System.err.println("Required layer " + requiredLayer + " is not available!");
            return false;
        }
        return true;
    }

    PointerBuffer getRequiredInstanceExtensions(MemoryStack stack, PointerBuffer glfwExtensions) {
        int numExtensions;
        if (DEBUG) {
            numExtensions = glfwExtensions.capacity() + REQUIRED_INSTANCE_BASE_EXTENSIONS.length
                    + REQUIRED_INSTANCE_DEBUG_EXTENSIONS.length;
        } else {
            numExtensions = glfwExtensions.capacity() + REQUIRED_INSTANCE_BASE_EXTENSIONS.length;
        }
        PointerBuffer requiredExtensions = stack.callocPointer(numExtensions);
        requiredExtensions.put(glfwExtensions);
        for (String baseExtensionName : REQUIRED_INSTANCE_BASE_EXTENSIONS) {
            requiredExtensions.put(stack.UTF8(baseExtensionName));
        }
        if (DEBUG) {
            for (String debugExtensionName : REQUIRED_INSTANCE_DEBUG_EXTENSIONS) {
                requiredExtensions.put(stack.UTF8(debugExtensionName));
            }
        }
        requiredExtensions.flip();
        return requiredExtensions;
    }

    String[] getRequiredDeviceExtensions() {
        final String[] requiredExtensionNames;
        if (DEBUG) {
            requiredExtensionNames = new String[REQUIRED_DEVICE_BASE_EXTENSIONS.length
                    + REQUIRED_DEVICE_DEBUG_EXTENSIONS.length];
        } else {
            requiredExtensionNames = new String[REQUIRED_DEVICE_BASE_EXTENSIONS.length];
        }
        System.arraycopy(REQUIRED_DEVICE_BASE_EXTENSIONS, 0, requiredExtensionNames, 0,
                REQUIRED_DEVICE_BASE_EXTENSIONS.length);
        if (DEBUG) {
            System.arraycopy(REQUIRED_DEVICE_DEBUG_EXTENSIONS, 0, requiredExtensionNames,
                    REQUIRED_DEVICE_BASE_EXTENSIONS.length, REQUIRED_DEVICE_DEBUG_EXTENSIONS.length);
        }
        return requiredExtensionNames;
    }

    void debugCallback(int severity, int messageTypes, long pCallbackData, long pUserData) {
        System.out.println("severity is " + severity + " and messageTypes is " + messageTypes);
        System.out.println(
                "The message is " + VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData).pMessageString());
    }

    void createInstance() {
        if (DEBUG && !checkValidationLayerSupport()) {
            throw new UnsupportedOperationException("Not all required validation layers are supported");
        }

        try (MemoryStack stack = stackPush()) {
            next("createInstance appInfo");
            VkApplicationInfo appInfo = VkApplicationInfo.callocStack(stack);
            appInfo.sType(VK10.VK_STRUCTURE_TYPE_APPLICATION_INFO);
            appInfo.pApplicationName(stack.UTF8("Hello Triangle"));
            appInfo.applicationVersion(VK10.VK_MAKE_VERSION(1, 0, 0));
            appInfo.pEngineName(stack.UTF8("No Engine"));
            appInfo.engineVersion(VK10.VK_MAKE_VERSION(1, 0, 0));
            appInfo.apiVersion(VK10.VK_API_VERSION_1_0);

            next("createInstance createInfo");
            VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.callocStack(stack);
            createInfo.sType(VK10.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
            createInfo.pApplicationInfo(appInfo);

            next("createInstance call glfwVulkanSupported()");
            if (!GLFWVulkan.glfwVulkanSupported()) {
                throw new UnsupportedOperationException("Vulkan is not supported");
            }

            next("createInstance get required glfw extensions");
            PointerBuffer requiredGLFWExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions();
            if (requiredGLFWExtensions == null) {
                throw new UnsupportedOperationException("No extensions for window surface creation were found");
            }

            next("createInstance enumerating extension properties");
            IntBuffer supportedExtensionCount = stack.callocInt(1);
            validate(VK10.vkEnumerateInstanceExtensionProperties((ByteBuffer) null, supportedExtensionCount, null));
            // VkExtensionProperties.Buffer supportedExtensions =
            // VkExtensionProperties.create(supportedExtensionCount[0]);
            VkExtensionProperties.Buffer supportedExtensions = VkExtensionProperties
                    .callocStack(supportedExtensionCount.get(0), stack);
            validate(VK10.vkEnumerateInstanceExtensionProperties((ByteBuffer) null, supportedExtensionCount,
                    supportedExtensions));
            next("createInstance print supported extensions");
            System.out.println("Supported extensions are:");
            supportedExtensions.forEach((VkExtensionProperties extension) -> {
                System.out.println(extension.extensionNameString());
            });
            System.out.println();
            System.out.println();

            next("createInstance get required instance extensions");

            PointerBuffer requiredExtensionsBuffer = getRequiredInstanceExtensions(stack, requiredGLFWExtensions);

            next("createInstance add required extensions and layers to createInfo");
            createInfo.ppEnabledExtensionNames(requiredExtensionsBuffer);
            createInfo.ppEnabledLayerNames(getLayersToEnable(stack));

            next("createInstance create the instance");
            PointerBuffer pInstance = stack.callocPointer(1);
            validate(VK10.vkCreateInstance(createInfo, null, pInstance));
            instance = new VkInstance(pInstance.get(0), createInfo);
            Performance.end();

            System.out.println("instance is " + instance);
        }
    }

    PointerBuffer getLayersToEnable(MemoryStack stack) {
        if (DEBUG) {
            PointerBuffer enabledLayerNames = stack.callocPointer(REQUIRED_DEBUG_LAYERS.length);
            for (String requiredLayer : REQUIRED_DEBUG_LAYERS) {
                enabledLayerNames.put(stack.UTF8(requiredLayer));
            }
            enabledLayerNames.flip();
            return enabledLayerNames;
        } else {
            return null;
        }
    }

    void setupDebugMessenger() {
        if (DEBUG) {
            try (MemoryStack stack = stackPush()) {
                VkDebugUtilsMessengerCreateInfoEXT createInfo = VkDebugUtilsMessengerCreateInfoEXT.callocStack(stack);
                populateDebugMessengerCreateInfo(createInfo);

                LongBuffer messengerAddress = stack.callocLong(1);
                validate(EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(instance, createInfo, null, messengerAddress));
                debugMessenger = messengerAddress.get(0);
            }
        }
    }

    void createSurface() {
        try (MemoryStack stack = stackPush()) {
            LongBuffer surfacePointer = stack.callocLong(1);
            validate(GLFWVulkan.glfwCreateWindowSurface(instance, window, null, surfacePointer));
            surface = surfacePointer.get(0);
        }
    }

    void populateDebugMessengerCreateInfo(VkDebugUtilsMessengerCreateInfoEXT createInfo) {
        createInfo.sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);

        createInfo.messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT
                | VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT);

        createInfo.messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT
                | VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT);

        VkDebugUtilsMessengerCallbackEXTI debugCallback = (int severity, int messageTypes, long pCallbackData,
                                                           long pUserData) -> {
            debugCallback(severity, messageTypes, pCallbackData, pUserData);
            return VK10.VK_FALSE;
        };

        createInfo.pfnUserCallback(debugCallback);
        createInfo.pUserData(NULL);
    }

    void pickPhysicalDevice() {
        // final int deviceCount;
        // final PointerBuffer devices;

        // {
        try (MemoryStack stack = stackPush()) {
            IntBuffer deviceCountBuffer = stack.callocInt(1);
            validate(VK10.vkEnumeratePhysicalDevices(instance, deviceCountBuffer, null));
            int deviceCount = deviceCountBuffer.get(0);

            if (deviceCount == 0) {
                throw new UnsupportedOperationException("No physical devices with Vulkan support found!");
            }

            PointerBuffer devices = stack.callocPointer(deviceCount);
            validate(VK10.vkEnumeratePhysicalDevices(instance, deviceCountBuffer, devices));
            // }

            for (int deviceIndex = 0; deviceIndex < deviceCount; deviceIndex++) {
                long devicePointer = devices.get(deviceIndex);
                VkPhysicalDevice currentDevice = new VkPhysicalDevice(devicePointer, instance);
                if (isSuitable(stack, currentDevice)) {
                    physicalDevice = currentDevice;
                }
            }
        }

        if (physicalDevice == null) {
            throw new UnsupportedOperationException("No suitable physical device for this application was found");
        }
    }

    boolean isSuitable(MemoryStack stack, VkPhysicalDevice device) {

        // We don't need the capabilities (yet)
        //VKCapabilitiesInstance capabilities = device.getCapabilities();

        if (findQueueFamilies(stack, device).isComplete() && checkExtensionSupport(device)) {
            boolean swapchainSupport = false;
            Swapchain swapchain = getSwapchainDetails(device, stack);
            swapchainSupport = swapchain.presentModes.capacity() > 0 && swapchain.surfaceFormats.capacity() > 0;

            if (swapchainSupport) {
                VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.create();
                VK10.vkGetPhysicalDeviceFeatures(device, features);
                return features.samplerAnisotropy();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    VkSurfaceFormatKHR chooseSwapSurfaceFormat(VkSurfaceFormatKHR.Buffer formats) {

        // Search a format that has the ideal settings
        for (VkSurfaceFormatKHR format : formats) {
            if (format.format() == VK10.VK_FORMAT_B8G8R8A8_UNORM
                    && format.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
                return format;
            }
        }

        // If we can't find it, we just return the first one
        return formats.get(0);
    }

    int chooseSwapPresentMode(IntBuffer presentModes) {

        // The next loop shows how to search for preferred display modes, but is now
        // commented out
        /*
         * for (int index = 0; index < presentModes.capacity(); index++) { int
         * presentMode = presentModes.get(index); if (presentMode ==
         * KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR) { return presentMode; } }
         */

        // For now, I will stick with FIFO, that is guaranteed to be available
        return KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
    }

    VkExtent2D chooseSwapExtent(MemoryStack stack, VkSurfaceCapabilitiesKHR caps) {
        int currentWidth = caps.currentExtent().width();

        // If currentWidth is the max value of uint32, it indicates a dynamic width
        if (currentWidth != UINT_MAX) {
            return caps.currentExtent();
        } else {
            VkExtent2D actualExtent = VkExtent2D.callocStack(stack);
            IntBuffer widthBuffer = stack.callocInt(1);
            IntBuffer heightBuffer = stack.callocInt(1);
            GLFW.glfwGetFramebufferSize(window, widthBuffer, heightBuffer);
            actualExtent.width(widthBuffer.get(0));
            actualExtent.height(heightBuffer.get(0));
            return actualExtent;
        }
    }

    void createSwapchain() {
        try (MemoryStack stack = stackPush()) {
            Swapchain details = getSwapchainDetails(physicalDevice, stack);
            VkSurfaceFormatKHR surfaceFormat = chooseSwapSurfaceFormat(details.surfaceFormats);
            int presentMode = chooseSwapPresentMode(details.presentModes);

            swapchainImageExtent = VkExtent2D.calloc().set(chooseSwapExtent(stack, details.caps));

            int imageCount = details.caps.minImageCount();
            int maxImageCount = details.caps.maxImageCount();

            // A maxImageCount of 0 indicates that there is no maximum
            if (maxImageCount == 0 || maxImageCount > imageCount) {
                imageCount++;
            }

            swapchainImageFormat = surfaceFormat.format();

            VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.callocStack(stack);
            createInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
            createInfo.surface(surface);
            createInfo.minImageCount(imageCount);
            createInfo.imageFormat(swapchainImageFormat);
            createInfo.imageColorSpace(surfaceFormat.colorSpace());
            createInfo.imageExtent(swapchainImageExtent);
            createInfo.imageArrayLayers(1);
            createInfo.imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);

            QueueFamilyIndices indices = findQueueFamilies(stack, physicalDevice);
            IntBuffer queueFamilyIndices = stack.ints(indices.graphicsFamily, indices.presentFamily);
            if (indices.graphicsFamily != indices.presentFamily) {
                createInfo.imageSharingMode(VK10.VK_SHARING_MODE_CONCURRENT);
                createInfo.pQueueFamilyIndices(queueFamilyIndices);
            } else {
                createInfo.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
                createInfo.pQueueFamilyIndices(null);
            }

            createInfo.preTransform(details.caps.currentTransform());
            createInfo.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
            createInfo.presentMode(presentMode);
            createInfo.clipped(true);
            createInfo.oldSwapchain(VK10.VK_NULL_HANDLE);

            LongBuffer swapchainBuffer = stack.callocLong(1);
            validate(KHRSwapchain.vkCreateSwapchainKHR(device, createInfo, null, swapchainBuffer));
            swapchain = swapchainBuffer.get(0);

            IntBuffer imageCountBuffer = stack.callocInt(1);
            validate(KHRSwapchain.vkGetSwapchainImagesKHR(device, swapchain, imageCountBuffer, null));
            swapchainImages = MemoryUtil.memAllocLong(imageCountBuffer.get(0));
            validate(KHRSwapchain.vkGetSwapchainImagesKHR(device, swapchain, imageCountBuffer, swapchainImages));
        }
    }

    boolean checkExtensionSupport(VkPhysicalDevice device) {
        final int extensionCount;
        final VkExtensionProperties.Buffer extensionProperties;
        {
            int[] extensionCountPointer = { -1 };
            VK10.vkEnumerateDeviceExtensionProperties(device, (ByteBuffer) null, extensionCountPointer, null);
            extensionCount = extensionCountPointer[0];
            extensionProperties = VkExtensionProperties.create(extensionCount);
            VK10.vkEnumerateDeviceExtensionProperties(device, (ByteBuffer) null, extensionCountPointer,
                    extensionProperties);
        }

        String[] requiredExtensionNames = getRequiredDeviceExtensions();

        boolean[] hasRequiredExtensions = new boolean[requiredExtensionNames.length];

        extensionProperties.forEach((VkExtensionProperties available) -> {
            String availableName = available.extensionNameString();
            for (int index = 0; index < requiredExtensionNames.length; index++) {
                if (!hasRequiredExtensions[index] && availableName.equals(requiredExtensionNames[index])) {
                    hasRequiredExtensions[index] = true;
                    break;
                }
            }
        });

        for (boolean has : hasRequiredExtensions) {
            if (!has) {
                return false;
            }
        }

        return true;
    }

    Swapchain getSwapchainDetails(VkPhysicalDevice device, MemoryStack stack) {
        VkSurfaceCapabilitiesKHR caps = VkSurfaceCapabilitiesKHR.callocStack(stack);
        validate(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, caps));

        IntBuffer countBuffer = stack.callocInt(1);
        validate(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, countBuffer, null));
        VkSurfaceFormatKHR.Buffer surfaceFormats = VkSurfaceFormatKHR.callocStack(countBuffer.get(0), stack);
        validate(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, countBuffer, surfaceFormats));

        validate(KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, countBuffer, null));
        IntBuffer presentModes = stack.callocInt(countBuffer.get(0));
        validate(KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, countBuffer, presentModes));

        return new Swapchain(caps, surfaceFormats, presentModes);
    }

    static class Swapchain {

        final VkSurfaceCapabilitiesKHR caps;
        final VkSurfaceFormatKHR.Buffer surfaceFormats;
        final IntBuffer presentModes;

        Swapchain(VkSurfaceCapabilitiesKHR caps, VkSurfaceFormatKHR.Buffer surfaceFormats, IntBuffer presentModes) {
            this.caps = caps;
            this.surfaceFormats = surfaceFormats;
            this.presentModes = presentModes;
        }
    }

    static class QueueFamilyIndices {

        int graphicsFamily;
        int presentFamily;

        QueueFamilyIndices() {
            graphicsFamily = -1;
            presentFamily = -1;
        }

        @Override
        public String toString() {
            return "{graphicsFamily: " + graphicsFamily + ", presentFamily: " + presentFamily + "}";
        }

        boolean isComplete() {
            return graphicsFamily >= 0 && presentFamily >= 0;
        }
    }

    QueueFamilyIndices findQueueFamilies(MemoryStack stack, VkPhysicalDevice device) {
        return findQueueFamilies(stack, device, VK10.VK_QUEUE_GRAPHICS_BIT);
    }

    QueueFamilyIndices findQueueFamilies(MemoryStack stack, VkPhysicalDevice device, int flag) {
        // final int queueFamilyCount;
        final VkQueueFamilyProperties.Buffer queueFamilyProperties;
        {
            IntBuffer queueFamilyCountPointer = stack.callocInt(1);
            VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCountPointer, null);
            // queueFamilyCount = queueFamilyCountPointer.get(0);
            queueFamilyProperties = VkQueueFamilyProperties.callocStack(queueFamilyCountPointer.get(0), stack);
            VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCountPointer, queueFamilyProperties);
        }

        QueueFamilyIndices families = new QueueFamilyIndices();

        int[] index = { 0 };
        queueFamilyProperties.forEach((VkQueueFamilyProperties family) -> {
            if (family.queueCount() > 0) {
                if ((family.queueFlags() & flag) == 1) {
                    families.graphicsFamily = index[0];
                }
                int[] supportedPointer = { -1 };
                validate(KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(device, index[0], surface, supportedPointer));
                if (supportedPointer[0] == VK10.VK_TRUE) {
                    families.presentFamily = index[0];
                }
            }
            index[0]++;
        });

        return families;
    }

    void createImageViews() {
        int size = swapchainImages.capacity();
        swapchainImageViews = MemoryUtil.memAllocLong(size);

        for (int index = 0; index < size; index++) {
            swapchainImageViews.put(index, createImageView(swapchainImages.get(index), swapchainImageFormat, VK10.VK_IMAGE_ASPECT_COLOR_BIT, 1));
        }
    }

    void createRenderPass() {
        try (MemoryStack stack = stackPush()) {
            VkAttachmentDescription depthAttachment = VkAttachmentDescription.callocStack(stack);
            depthAttachment.format(findDepthFormat());
            depthAttachment.samples(VK10.VK_SAMPLE_COUNT_1_BIT);
            depthAttachment.loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR);
            depthAttachment.storeOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE);
            depthAttachment.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            depthAttachment.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE);
            depthAttachment.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED);
            depthAttachment.finalLayout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            VkAttachmentDescription colorAttachment = VkAttachmentDescription.callocStack(stack);
            colorAttachment.format(swapchainImageFormat);
            // TODO Sample count is interesting for multisampling, which is needed for
            // anti-aliasing
            colorAttachment.samples(VK10.VK_SAMPLE_COUNT_1_BIT);
            // TODO The loadOP is interesting for preserving parts of the screen
            colorAttachment.loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR);
            colorAttachment.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE);
            colorAttachment.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            colorAttachment.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE);
            colorAttachment.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED);
            colorAttachment.finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

            VkAttachmentReference depthAttachmentRef = VkAttachmentReference.callocStack(stack);
            depthAttachmentRef.attachment(1);
            depthAttachmentRef.layout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            VkAttachmentReference attachmentRef = VkAttachmentReference.callocStack(stack);
            attachmentRef.attachment(0);
            attachmentRef.layout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkSubpassDescription subpass = VkSubpassDescription.callocStack(stack);
            subpass.pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS);
            subpass.colorAttachmentCount(1);
            subpass.pColorAttachments(VkAttachmentReference.callocStack(1, stack).put(attachmentRef).flip());
            subpass.pDepthStencilAttachment(depthAttachmentRef);

            VkSubpassDependency dependency = VkSubpassDependency.callocStack(stack);
            dependency.srcSubpass(VK10.VK_SUBPASS_EXTERNAL);
            // 0 is the index of our only subpass
            dependency.dstSubpass(0);

            dependency.srcStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
            dependency.srcAccessMask(0);

            dependency.dstStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
            dependency.dstAccessMask(
                    VK10.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

            VkRenderPassCreateInfo renderPassCI = VkRenderPassCreateInfo.callocStack(stack);
            renderPassCI.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
            renderPassCI.pAttachments(VkAttachmentDescription.callocStack(2, stack).put(0, colorAttachment).put(1, depthAttachment));
            renderPassCI.pSubpasses(VkSubpassDescription.callocStack(1, stack).put(subpass).flip());

            renderPassCI.pDependencies(VkSubpassDependency.callocStack(1, stack).put(dependency).flip());

            LongBuffer renderPassResult = stack.callocLong(1);
            validate(VK10.vkCreateRenderPass(device, renderPassCI, null, renderPassResult));
            renderPass = renderPassResult.get(0);
        }
    }

    void createDescriptorSetLayout() {
        try (MemoryStack stack = stackPush()){
            VkDescriptorSetLayoutBinding uboLayoutBinding = VkDescriptorSetLayoutBinding.callocStack(stack);
            uboLayoutBinding.binding(0);
            uboLayoutBinding.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            uboLayoutBinding.descriptorCount(1);

            uboLayoutBinding.stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT);
            uboLayoutBinding.pImmutableSamplers(null);

            VkDescriptorSetLayoutBinding samplerLayoutBinding = VkDescriptorSetLayoutBinding.callocStack(stack);
            samplerLayoutBinding.binding(1);
            samplerLayoutBinding.descriptorCount(1);
            samplerLayoutBinding.descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
            samplerLayoutBinding.pImmutableSamplers(null);
            samplerLayoutBinding.stageFlags(VK10.VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutCreateInfo descriptorCI = VkDescriptorSetLayoutCreateInfo.callocStack(stack);
            descriptorCI.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            descriptorCI.pBindings(VkDescriptorSetLayoutBinding.callocStack(2, stack).put(0, uboLayoutBinding).put(1, samplerLayoutBinding));

            LongBuffer pSetLayout = stack.callocLong(1);
            validate(VK10.vkCreateDescriptorSetLayout(device, descriptorCI, null, pSetLayout));
            descriptorSetLayout = pSetLayout.get(0);
        }
    }

    void createGraphicsPipeline() {
        byte[] vertexCode = readFile("nl/knokko/test5/vert.spv");
        byte[] fragmentCode = readFile("nl/knokko/test5/frag.spv");

        long vertShaderModule = createShaderModule(vertexCode);
        long fragShaderModule = createShaderModule(fragmentCode);

        try (MemoryStack stack = stackPush()) {
            VkPipelineShaderStageCreateInfo vertexCI = VkPipelineShaderStageCreateInfo.callocStack(stack);
            vertexCI.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
            vertexCI.stage(VK10.VK_SHADER_STAGE_VERTEX_BIT);
            vertexCI.module(vertShaderModule);
            vertexCI.pName(stack.UTF8("main"));

            VkPipelineShaderStageCreateInfo fragmentCI = VkPipelineShaderStageCreateInfo.callocStack(stack);
            fragmentCI.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
            fragmentCI.stage(VK10.VK_SHADER_STAGE_FRAGMENT_BIT);
            fragmentCI.module(fragShaderModule);
            fragmentCI.pName(stack.UTF8("main"));

            VkPipelineVertexInputStateCreateInfo vertexInputCI = VkPipelineVertexInputStateCreateInfo
                    .callocStack(stack);
            vertexInputCI.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
            vertexInputCI.pVertexBindingDescriptions(Vertex.getBindingDescription(stack));
            vertexInputCI.pVertexAttributeDescriptions(Vertex.getAttributeDescriptions(stack));

            VkPipelineInputAssemblyStateCreateInfo assemblyCI = VkPipelineInputAssemblyStateCreateInfo
                    .callocStack(stack);
            assemblyCI.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
            assemblyCI.topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
            assemblyCI.primitiveRestartEnable(false);

            VkViewport viewport = VkViewport.callocStack(stack);
            viewport.x(0f);
            viewport.y(0f);
            viewport.width((float) swapchainImageExtent.width());
            viewport.height((float) swapchainImageExtent.height());
            viewport.minDepth(0f);
            viewport.maxDepth(1f);

            VkRect2D scissor = VkRect2D.callocStack(stack);
            scissor.offset(VkOffset2D.callocStack(stack).set(0, 0));
            scissor.extent(swapchainImageExtent);

            VkPipelineViewportStateCreateInfo viewportCI = VkPipelineViewportStateCreateInfo.callocStack(stack);
            viewportCI.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
            viewportCI.viewportCount(1);
            viewportCI.scissorCount(1);
            viewportCI.pViewports(VkViewport.callocStack(1, stack).put(viewport).flip());
            viewportCI.pScissors(VkRect2D.callocStack(1, stack).put(scissor).flip());

            VkPipelineRasterizationStateCreateInfo rasterCI = VkPipelineRasterizationStateCreateInfo.callocStack(stack);
            rasterCI.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
            rasterCI.depthClampEnable(false);
            rasterCI.rasterizerDiscardEnable(false);
            rasterCI.polygonMode(VK10.VK_POLYGON_MODE_FILL);
            rasterCI.lineWidth(1f);

            rasterCI.cullMode(VK10.VK_CULL_MODE_BACK_BIT);

            rasterCI.frontFace(VK10.VK_FRONT_FACE_CLOCKWISE);
            rasterCI.depthBiasEnable(false);
            rasterCI.depthBiasConstantFactor(0f);
            rasterCI.depthBiasClamp(0f);
            rasterCI.depthBiasSlopeFactor(0f);

            // TODO Below is where the anti-aliasing could be enabled
            VkPipelineMultisampleStateCreateInfo sampleCI = VkPipelineMultisampleStateCreateInfo.callocStack(stack);
            sampleCI.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
            sampleCI.sampleShadingEnable(false);
            sampleCI.rasterizationSamples(VK10.VK_SAMPLE_COUNT_1_BIT);
            sampleCI.minSampleShading(1f);
            sampleCI.pSampleMask(null);
            sampleCI.alphaToCoverageEnable(false);
            sampleCI.alphaToOneEnable(false);

            VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.callocStack(stack);
            depthStencil.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
            depthStencil.depthTestEnable(true);
            depthStencil.depthWriteEnable(true);
            depthStencil.depthCompareOp(VK10.VK_COMPARE_OP_LESS);
            depthStencil.depthBoundsTestEnable(false);
            depthStencil.stencilTestEnable(false);

            VkPipelineColorBlendAttachmentState blendAttachment = VkPipelineColorBlendAttachmentState
                    .callocStack(stack);
            blendAttachment.colorWriteMask(VK10.VK_COLOR_COMPONENT_R_BIT | VK10.VK_COLOR_COMPONENT_G_BIT
                    | VK10.VK_COLOR_COMPONENT_B_BIT | VK10.VK_COLOR_COMPONENT_A_BIT);
            blendAttachment.blendEnable(false);
            blendAttachment.srcColorBlendFactor(VK10.VK_BLEND_FACTOR_ONE);
            blendAttachment.dstColorBlendFactor(VK10.VK_BLEND_FACTOR_ZERO);
            blendAttachment.colorBlendOp(VK10.VK_BLEND_OP_ADD);
            blendAttachment.srcAlphaBlendFactor(VK10.VK_BLEND_FACTOR_ONE);
            blendAttachment.dstAlphaBlendFactor(VK10.VK_BLEND_FACTOR_ZERO);
            blendAttachment.alphaBlendOp(VK10.VK_BLEND_OP_ADD);

            // In case you want blending:
            // blendAttachmentCI.blendEnable(true);
            // blendAttachmentCI.srcColorBlendFactor(VK10.VK_BLEND_FACTOR_SRC_ALPHA);
            // blendAttachmentCI.dstColorBlendFactor(VK10.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA);
            // blendAttachmentCI.colorBlendOp(VK10.VK_BLEND_OP_ADD);
            // blendAttachmentCI.srcAlphaBlendFactor(VK10.VK_BLEND_FACTOR_ONE);
            // blendAttachmentCI.dstAlphaBlendFactor(VK10.VK_BLEND_FACTOR_ZERO);
            // blendAttachmentCI.alphaBlendOp(VK10.VK_BLEND_OP_ADD);

            VkPipelineColorBlendStateCreateInfo blendStateCI = VkPipelineColorBlendStateCreateInfo.callocStack(stack);
            blendStateCI.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
            blendStateCI.logicOpEnable(false);
            blendStateCI.logicOp(VK10.VK_LOGIC_OP_COPY);
            blendStateCI.pAttachments(
                    VkPipelineColorBlendAttachmentState.callocStack(1, stack).put(blendAttachment).flip());
            blendStateCI.blendConstants(stack.floats(0f, 0f, 0f, 0f));

            VkPipelineLayoutCreateInfo layoutCI = VkPipelineLayoutCreateInfo.callocStack(stack);
            layoutCI.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
            layoutCI.pSetLayouts(stack.longs(descriptorSetLayout));
            layoutCI.pPushConstantRanges(null);
            LongBuffer pipelineResultBuffer = stack.callocLong(1);
            validate(VK10.vkCreatePipelineLayout(device, layoutCI, null, pipelineResultBuffer));
            pipelineLayout = pipelineResultBuffer.get(0);

            VkGraphicsPipelineCreateInfo pipelineCI = VkGraphicsPipelineCreateInfo.callocStack(stack);
            pipelineCI.sType(VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
            pipelineCI.pStages(
                    VkPipelineShaderStageCreateInfo.callocStack(2, stack).put(vertexCI).put(fragmentCI).flip());
            pipelineCI.pVertexInputState(vertexInputCI);
            pipelineCI.pInputAssemblyState(assemblyCI);
            pipelineCI.pViewportState(viewportCI);
            pipelineCI.pRasterizationState(rasterCI);
            pipelineCI.pMultisampleState(sampleCI);
            pipelineCI.pDepthStencilState(depthStencil);
            pipelineCI.pColorBlendState(blendStateCI);
            pipelineCI.pDynamicState(null);
            pipelineCI.layout(pipelineLayout);
            pipelineCI.renderPass(renderPass);
            pipelineCI.subpass(0);

            // TODO Using a non-null handle can be used to recreate pipelines
            pipelineCI.basePipelineHandle(VK_NULL_HANDLE);
            pipelineCI.basePipelineIndex(-1);

            LongBuffer pipelineResult = stack.callocLong(1);

            // TODO Using non-null handle can be used to speed up creation and larger
            // buffers could be used
            validate(VK10.vkCreateGraphicsPipelines(device, VK_NULL_HANDLE,
                    VkGraphicsPipelineCreateInfo.callocStack(1, stack).put(pipelineCI).flip(), null, pipelineResult));
            graphicsPipeline = pipelineResult.get(0);
        }

        VK10.vkDestroyShaderModule(device, vertShaderModule, null);
        VK10.vkDestroyShaderModule(device, fragShaderModule, null);
    }

    void createFramebuffers() {
        swapchainFrameBuffers = MemoryUtil.memAllocLong(swapchainImageViews.capacity());
        for (int index = 0; index < swapchainFrameBuffers.capacity(); index++) {
            try (MemoryStack stack = stackPush()) {
                VkFramebufferCreateInfo framebufferCI = VkFramebufferCreateInfo.callocStack(stack);
                framebufferCI.sType(VK10.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
                framebufferCI.renderPass(renderPass);
                framebufferCI.pAttachments(stack.longs(swapchainImageViews.get(index), depthImageView));
                framebufferCI.width(swapchainImageExtent.width());
                framebufferCI.height(swapchainImageExtent.height());
                framebufferCI.layers(1);
                validate(VK10.vkCreateFramebuffer(device, framebufferCI, null,
                        (LongBuffer) swapchainFrameBuffers.position(index)));
            }
        }
        swapchainFrameBuffers.position(0);
    }

    void createCommandPool() {
        try (MemoryStack stack = stackPush()) {
            QueueFamilyIndices queueFamilyIndices = findQueueFamilies(stack, physicalDevice);

            VkCommandPoolCreateInfo commandPoolCI = VkCommandPoolCreateInfo.callocStack(stack);
            commandPoolCI.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
            commandPoolCI.queueFamilyIndex(queueFamilyIndices.graphicsFamily);
            commandPoolCI.flags(0);
            LongBuffer commandPoolResultBuffer = stack.callocLong(1);
            validate(VK10.vkCreateCommandPool(device, commandPoolCI, null, commandPoolResultBuffer));
            this.commandPool = commandPoolResultBuffer.get(0);
        }
    }

    void createDepthResources() {
        int format = findDepthFormat();
        System.out.println("Depth format is " + format);

        try (MemoryStack stack = stackPush()){
            LongBuffer pImage = stack.callocLong(1);
            LongBuffer pMemory = stack.callocLong(1);
            createImage(swapchainImageExtent.width(), swapchainImageExtent.height(), 1, format, VK10.VK_IMAGE_TILING_OPTIMAL,
                    VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pImage, pMemory);
            depthImage = pImage.get(0);
            depthImageMemory = pMemory.get(0);
        }

        depthImageView = createImageView(depthImage, format, VK10.VK_IMAGE_ASPECT_DEPTH_BIT, 1);

        transitionImageLayout(depthImage, format, VK10.VK_FORMAT_UNDEFINED, VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL, 1);
    }

    boolean hasStencilComponent(int format) {
        return format == VK10.VK_FORMAT_D32_SFLOAT_S8_UINT || format == VK10.VK_FORMAT_D24_UNORM_S8_UINT;
    }

    int findDepthFormat() {
        return findSupportedFormat(VK10.VK_IMAGE_TILING_OPTIMAL, VK10.VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT,
                VK10.VK_FORMAT_D32_SFLOAT, VK10.VK_FORMAT_D32_SFLOAT_S8_UINT, VK10.VK_FORMAT_D24_UNORM_S8_UINT);
    }

    int findSupportedFormat(int tiling, int features, int...candidates) {
        for (int candidate : candidates) {
            try (MemoryStack stack = stackPush()){
                VkFormatProperties props = VkFormatProperties.callocStack(stack);
                VK10.vkGetPhysicalDeviceFormatProperties(physicalDevice, candidate, props);

                if (tiling == VK10.VK_IMAGE_TILING_LINEAR && (props.linearTilingFeatures() & features) == features) {
                    return candidate;
                } else if (tiling == VK10.VK_IMAGE_TILING_OPTIMAL && (props.optimalTilingFeatures() & features) == features) {
                    return candidate;
                }
            }
        }
        throw new RuntimeException("No candidate was suitable");
    }

    void createImage(int width, int height, int mipLevels, int format, int tiling, int usage, int properties, LongBuffer pImage, LongBuffer pImageMemory) {
        try (MemoryStack stack = stackPush()){
            VkImageCreateInfo imageCI = VkImageCreateInfo.callocStack(stack);
            imageCI.sType(VK10.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO);
            imageCI.imageType(VK10.VK_IMAGE_TYPE_2D);
            imageCI.extent(VkExtent3D.callocStack(stack).set(width, height, 1));
            imageCI.mipLevels(mipLevels);
            imageCI.arrayLayers(1);
            imageCI.format(format);

            // TODO For mutable images, use VK_IMAGE_TILING_LINEAR instead
            imageCI.tiling(tiling);
            imageCI.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED);
            imageCI.usage(usage);
            imageCI.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
            imageCI.samples(VK10.VK_SAMPLE_COUNT_1_BIT);
            imageCI.flags(0);

            validate(VK10.vkCreateImage(device, imageCI, null, pImage));

            VkMemoryRequirements memRequirements = VkMemoryRequirements.callocStack(stack);
            VK10.vkGetImageMemoryRequirements(device, pImage.get(0), memRequirements);

            VkMemoryAllocateInfo memoryAI = VkMemoryAllocateInfo.callocStack(stack);
            memoryAI.sType(VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
            memoryAI.allocationSize(memRequirements.size());
            memoryAI.memoryTypeIndex(findMemoryType(memRequirements.memoryTypeBits(), properties));

            validate(VK10.vkAllocateMemory(device, memoryAI, null, pImageMemory));

            validate(VK10.vkBindImageMemory(device, pImage.get(0), pImageMemory.get(0), 0));
        }
    }

    static double log2(double number) {
        return Math.log10(number) / Math.log10(2.0);
    }

    void createTextureImage() {
        try (MemoryStack stack = stackPush()){
            BufferedImage image = ImageIO.read(TriangleTest.class.getClassLoader().getResource("nl/knokko/test5/statue.jpg"));
            mipLevels = (int) log2(Math.max(image.getWidth(), image.getHeight())) + 1;
            //BufferedImage image2 = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            LongBuffer stagingBuffer = stack.callocLong(1);
            LongBuffer stagingBufferMemory = stack.callocLong(1);
            int imageIntSize = image.getWidth() * image.getHeight();
            int imageByteSize = 4 * imageIntSize;
            createBuffer(imageByteSize, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                    stagingBuffer, stagingBufferMemory);
            PointerBuffer ppData = stack.callocPointer(1);
            validate(VK10.vkMapMemory(device, stagingBufferMemory.get(0), 0, imageByteSize, 0, ppData));
            ByteBuffer byteDestBuffer = MemoryUtil.memByteBuffer(ppData.get(0), imageByteSize);
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);

                    // DON'T USE byteDestBuffer.putInt(argb) because that will flip red and blue
                    Color color = new Color(argb);
                    byteDestBuffer.put((byte) color.getRed());
                    byteDestBuffer.put((byte) color.getGreen());
                    byteDestBuffer.put((byte) color.getBlue());
                    byteDestBuffer.put((byte) color.getAlpha());
                }
            }
            VK10.vkUnmapMemory(device, stagingBufferMemory.get(0));

            LongBuffer pTextureImage = stack.callocLong(1);
            LongBuffer pTextureImageMemory = stack.callocLong(1);
            createImage(image.getWidth(), image.getHeight(), mipLevels, VK10.VK_FORMAT_R8G8B8A8_UNORM,
                    VK10.VK_IMAGE_TILING_OPTIMAL,
                    VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK10.VK_IMAGE_USAGE_SAMPLED_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT,
                    VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, pTextureImage, pTextureImageMemory);
            textureImage = pTextureImage.get(0);
            textureImageMemory = pTextureImageMemory.get(0);

            transitionImageLayout(textureImage, VK10.VK_FORMAT_R8G8B8A8_UNORM, VK10.VK_IMAGE_LAYOUT_UNDEFINED,
                    VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, mipLevels);
            copyBufferToImage(stagingBuffer.get(0), textureImage, image.getWidth(), image.getHeight());

            generateMipmaps(textureImage, VK10.VK_FORMAT_R8G8B8A8_UNORM, image.getWidth(), image.getHeight(), mipLevels);

            VK10.vkDestroyBuffer(device, stagingBuffer.get(0), null);
            VK10.vkFreeMemory(device, stagingBufferMemory.get(0), null);
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    void generateMipmaps(long image, int imageFormat, int width, int height, int mipLevels) {
        VkCommandBuffer commandBuffer = beginSingleTimeCommands();

        try (MemoryStack stack = stackPush()){

            VkFormatProperties formatProps = VkFormatProperties.callocStack(stack);
            VK10.vkGetPhysicalDeviceFormatProperties(physicalDevice, imageFormat, formatProps);
            if ((formatProps.optimalTilingFeatures() & VK10.VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_LINEAR_BIT) == 0) {

                // TODO Better error handling, like pregenerating as file or a custom calculation method
                throw new RuntimeException("No support for linear image blitting");
            }

            VkImageMemoryBarrier barrier = VkImageMemoryBarrier.callocStack(stack);
            barrier.sType(VK10.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
            barrier.image(image);
            barrier.srcQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED);
            barrier.dstQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED);
            barrier.subresourceRange(VkImageSubresourceRange.callocStack(stack).set(VK10.VK_IMAGE_ASPECT_COLOR_BIT, -1, 1, 0, 1));

            int mipWidth = width;
            int mipHeight = height;

            VkOffset3D ZERO = VkOffset3D.callocStack(stack).set(0, 0, 0);
            for (int i = 1; i < mipLevels; i++) {
                barrier.subresourceRange().baseMipLevel(i - 1);
                barrier.oldLayout(VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
                barrier.newLayout(VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
                barrier.srcAccessMask(VK10.VK_ACCESS_TRANSFER_WRITE_BIT);
                barrier.dstAccessMask(VK10.VK_ACCESS_TRANSFER_READ_BIT);

                VK10.vkCmdPipelineBarrier(commandBuffer,
                        VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT,
                        0, null, null, VkImageMemoryBarrier.callocStack(1, stack).put(0, barrier));

                VkImageBlit blit = VkImageBlit.callocStack(stack);
                blit.srcOffsets(0, ZERO);
                blit.srcOffsets(1, VkOffset3D.callocStack(stack).set(mipWidth, mipHeight, 1));
                blit.srcSubresource(VkImageSubresourceLayers.callocStack(stack).set(VK10.VK_IMAGE_ASPECT_COLOR_BIT, i - 1, 0, 1));
                blit.dstOffsets(0, ZERO);
                blit.dstOffsets(1, VkOffset3D.callocStack(stack).set(mipWidth > 1 ? mipWidth / 2 : 1, mipHeight > 1 ? mipHeight / 2 : 1, 1));
                blit.dstSubresource(VkImageSubresourceLayers.callocStack(stack).set(VK10.VK_IMAGE_ASPECT_COLOR_BIT, i, 0, 1));

                // TODO Maybe use VK_FILTER_NEAREST in some circumstances?
                VK10.vkCmdBlitImage(commandBuffer, textureImage, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT,
                        textureImage, VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
                        VkImageBlit.callocStack(1, stack).put(0, blit), VK10.VK_FILTER_LINEAR);

                barrier.oldLayout(VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
                barrier.newLayout(VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
                barrier.srcAccessMask(VK10.VK_ACCESS_TRANSFER_READ_BIT);
                barrier.dstAccessMask(VK10.VK_ACCESS_SHADER_READ_BIT);

                VK10.vkCmdPipelineBarrier(commandBuffer,
                        VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
                        0, null, null, VkImageMemoryBarrier.callocStack(1, stack).put(0, barrier));

                if (mipWidth > 1) {
                    mipWidth /= 2;
                }
                if (mipHeight > 1) {
                    mipHeight /= 2;
                }
            }

            barrier.subresourceRange().baseMipLevel(mipLevels - 1);
            barrier.oldLayout(VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
            barrier.newLayout(VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            barrier.srcAccessMask(VK10.VK_ACCESS_TRANSFER_WRITE_BIT);
            barrier.dstAccessMask(VK10.VK_ACCESS_SHADER_READ_BIT);

            VK10.vkCmdPipelineBarrier(commandBuffer,
                    VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
                    0, null, null, VkImageMemoryBarrier.callocStack(1, stack).put(0, barrier));

            endSingleTimeCommands(commandBuffer);
        }
    }

    void createTextureImageView() {
        textureImageView = createImageView(textureImage, VK10.VK_FORMAT_R8G8B8A8_UNORM, VK10.VK_IMAGE_ASPECT_COLOR_BIT, mipLevels);
    }

    long createImageView(long image, int format, int aspectFlags, int mipLevels) {
        try (MemoryStack stack = stackPush()){
            VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.callocStack(stack);
            viewInfo.sType(VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
            viewInfo.image(image);
            viewInfo.viewType(VK10.VK_IMAGE_VIEW_TYPE_2D);
            viewInfo.format(format);
            viewInfo.subresourceRange(VkImageSubresourceRange.callocStack(stack).set(aspectFlags, 0, mipLevels, 0, 1));

            LongBuffer pView = stack.callocLong(1);
            validate(VK10.vkCreateImageView(device, viewInfo, null, pView));
            return pView.get(0);
        }
    }

    void createTextureSampler() {
        try (MemoryStack stack = stackPush()){
            VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.callocStack(stack);
            samplerInfo.sType(VK10.VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);

            // TODO Whatever is most appropriate
            samplerInfo.magFilter(VK10.VK_FILTER_NEAREST);
            samplerInfo.minFilter(VK10.VK_FILTER_LINEAR);

            samplerInfo.addressModeU(VK10.VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT);
            samplerInfo.addressModeV(VK10.VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT);
            samplerInfo.addressModeW(VK10.VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT);

            samplerInfo.anisotropyEnable(true);
            samplerInfo.maxAnisotropy(16);

            // Shouldn't matter because we use MIRRIORED_REPEAT
            samplerInfo.borderColor(VK10.VK_BORDER_COLOR_INT_OPAQUE_WHITE);

            samplerInfo.unnormalizedCoordinates(false);

            samplerInfo.compareEnable(false);
            samplerInfo.compareOp(VK10.VK_COMPARE_OP_ALWAYS);

            samplerInfo.mipmapMode(VK10.VK_SAMPLER_MIPMAP_MODE_LINEAR);
            samplerInfo.mipLodBias(0f);

            // TODO Play around with minLod for testing, but should generally be 0
            samplerInfo.minLod(0);
            samplerInfo.maxLod(mipLevels);

            LongBuffer pSampler = stack.callocLong(1);
            validate(VK10.vkCreateSampler(device, samplerInfo, null, pSampler));
            textureSampler = pSampler.get(0);
        }
    }

    VkCommandBuffer beginSingleTimeCommands() {
        try (MemoryStack stack = stackPush()){
            VkCommandBufferAllocateInfo commandBufferAI = VkCommandBufferAllocateInfo.callocStack(stack);
            commandBufferAI.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            commandBufferAI.level(VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            commandBufferAI.commandPool(commandPool);
            commandBufferAI.commandBufferCount(1);

            PointerBuffer pCommandBuffer = stack.callocPointer(1);
            validate(VK10.vkAllocateCommandBuffers(device, commandBufferAI, pCommandBuffer));
            VkCommandBuffer commandBuffer = new VkCommandBuffer(pCommandBuffer.get(0), device);

            VkCommandBufferBeginInfo commandBufferBI = VkCommandBufferBeginInfo.callocStack(stack);
            commandBufferBI.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            commandBufferBI.flags(VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

            validate(VK10.vkBeginCommandBuffer(commandBuffer, commandBufferBI));
            return commandBuffer;
        }
    }

    void endSingleTimeCommands(VkCommandBuffer commandBuffer) {
        try (MemoryStack stack = stackPush()){
            validate(VK10.vkEndCommandBuffer(commandBuffer));
            VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack);
            submitInfo.sType(VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.pCommandBuffers(stack.pointers(commandBuffer));

            validate(VK10.vkQueueSubmit(graphicsQueue, submitInfo, VK_NULL_HANDLE));
            validate(VK10.vkQueueWaitIdle(graphicsQueue));

            VK10.vkFreeCommandBuffers(device, commandPool, commandBuffer);
        }
    }

    void createBuffer(long byteSize, int usage, int properties, LongBuffer buffer, LongBuffer bufferMemory) {
        try (MemoryStack stack = stackPush()) {
            VkBufferCreateInfo bufferCI = VkBufferCreateInfo.callocStack(stack);
            bufferCI.sType(VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
            bufferCI.size(byteSize);
            bufferCI.usage(usage);
            bufferCI.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);

            validate(VK10.vkCreateBuffer(device, bufferCI, null, buffer));
        }
        try (MemoryStack stack = stackPush()) {
            VkMemoryRequirements memRequirements = VkMemoryRequirements.callocStack(stack);
            VK10.vkGetBufferMemoryRequirements(device, buffer.get(buffer.position()), memRequirements);

            VkMemoryAllocateInfo memoryAI = VkMemoryAllocateInfo.callocStack(stack);
            memoryAI.sType(VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
            memoryAI.allocationSize(memRequirements.size());
            memoryAI.memoryTypeIndex(findMemoryType(memRequirements.memoryTypeBits(), properties));

            // TODO Notice that this function should be used to create few big buffers rather than many small
            validate(VK10.vkAllocateMemory(device, memoryAI, null, bufferMemory));
        }
        validate(VK10.vkBindBufferMemory(device, buffer.get(buffer.position()), bufferMemory.get(bufferMemory.position()), 0));
    }

    void createVertexBuffers() {
        int byteSize = VERTICES.length * Vertex.BYTES;

        try (MemoryStack stack = stackPush()){
            LongBuffer stagingBuffer = stack.callocLong(1);
            LongBuffer stagingBufferMemory = stack.callocLong(1);
            createBuffer(byteSize, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                    stagingBuffer, stagingBufferMemory);

            PointerBuffer ppData = stack.callocPointer(1);
            validate(VK10.vkMapMemory(device, stagingBufferMemory.get(0), 0, byteSize, 0, ppData));

            FloatBuffer resultBuffer = MemoryUtil.memByteBuffer(ppData.get(0), VERTICES.length * Vertex.BYTES).asFloatBuffer();
            for (int vertexIndex = 0; vertexIndex < VERTICES.length; vertexIndex++)
                VERTICES[vertexIndex].put(resultBuffer, vertexIndex * Vertex.FLOATS);

            VK10.vkUnmapMemory(device, stagingBufferMemory.get(0));

            LongBuffer pVertexBuffer = stack.callocLong(1);
            LongBuffer pVertexBufferMemory = stack.callocLong(1);
            createBuffer(byteSize, VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                    VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, pVertexBuffer, pVertexBufferMemory);
            vertexBuffer = pVertexBuffer.get(0);
            vertexBufferMemory = pVertexBufferMemory.get(0);

            copyBuffer(stagingBuffer.get(0), vertexBuffer, byteSize);

            VK10.vkDestroyBuffer(device, stagingBuffer.get(0), null);
            VK10.vkFreeMemory(device, stagingBufferMemory.get(0), null);
        }
    }

    void createIndexBuffers() {
        int byteSize = INDEX_TYPE_SIZE * INDICES.length;

        try (MemoryStack stack = stackPush()){
            LongBuffer pStagingBuffer = stack.callocLong(1);
            LongBuffer pStagingBufferMemory = stack.callocLong(1);
            createBuffer(byteSize, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                    pStagingBuffer, pStagingBufferMemory);
            long stagingBufferMemory = pStagingBufferMemory.get(0);
            long stagingBuffer = pStagingBuffer.get(0);

            PointerBuffer ppData = stack.callocPointer(1);
            validate(VK10.vkMapMemory(device, stagingBufferMemory, 0, byteSize, 0, ppData));

            ShortBuffer memoryBuffer = MemoryUtil.memByteBuffer(ppData.get(0), byteSize).asShortBuffer();
            memoryBuffer.put(INDICES).flip();
            VK10.vkUnmapMemory(device, stagingBufferMemory);

            LongBuffer pIndexBuffer = stack.callocLong(1);
            LongBuffer pIndexBufferMemory = stack.callocLong(1);
            createBuffer(byteSize, VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, pIndexBuffer, pIndexBufferMemory);
            indexBuffer = pIndexBuffer.get(0);
            indexBufferMemory = pIndexBufferMemory.get(0);

            copyBuffer(stagingBuffer, indexBuffer, byteSize);

            VK10.vkDestroyBuffer(device, stagingBuffer, null);
            VK10.vkFreeMemory(device, stagingBufferMemory, null);
        }
    }

    void createUniformBuffers() {
        int bufferSize = UniformBufferObject.BYTES;
        int imageCount = swapchainImages.capacity();

        uniformBuffers = MemoryUtil.memCallocLong(imageCount);
        uniformBuffersMemory = MemoryUtil.memCallocLong(imageCount);

        for (int index = 0; index < swapchainImages.capacity(); index++) {
            createBuffer(bufferSize, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                    VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                    (LongBuffer) uniformBuffers.position(index), (LongBuffer) uniformBuffersMemory.position(index));
        }
    }

    void createDescriptorPool() {
        try (MemoryStack stack = stackPush()){
            VkDescriptorPoolSize poolSize1 = VkDescriptorPoolSize.callocStack(stack);
            poolSize1.type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            poolSize1.descriptorCount(swapchainImages.capacity());

            VkDescriptorPoolSize poolSize2 = VkDescriptorPoolSize.callocStack(stack);
            poolSize2.type(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
            poolSize2.descriptorCount(swapchainImages.capacity());

            VkDescriptorPoolCreateInfo poolCI = VkDescriptorPoolCreateInfo.callocStack(stack);
            poolCI.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
            poolCI.pPoolSizes(VkDescriptorPoolSize.callocStack(2, stack).put(0, poolSize1).put(1, poolSize2));
            poolCI.maxSets(swapchainImages.capacity());

            LongBuffer pDescriptorPool = stack.callocLong(1);
            validate(VK10.vkCreateDescriptorPool(device, poolCI, null, pDescriptorPool));
            descriptorPool = pDescriptorPool.get(0);
        }
    }

    void createDescriptorSets() {
        try (MemoryStack stack = stackPush()){
            VkDescriptorSetAllocateInfo descriptorSetAI = VkDescriptorSetAllocateInfo.callocStack(stack);
            descriptorSetAI.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
            descriptorSetAI.descriptorPool(descriptorPool);
            long[] descriptorSetLayoutCopies = new long[swapchainImages.capacity()];
            Arrays.fill(descriptorSetLayoutCopies, descriptorSetLayout);
            descriptorSetAI.pSetLayouts(stack.longs(descriptorSetLayoutCopies));

            LongBuffer descriptorSets = stack.callocLong(swapchainImages.capacity());
            validate(VK10.vkAllocateDescriptorSets(device, descriptorSetAI, descriptorSets));
            this.descriptorSets = new long[swapchainImages.capacity()];
            descriptorSets.get(this.descriptorSets);

            for (int index = 0; index < swapchainImages.capacity(); index++) {
                VkDescriptorBufferInfo descriptorBI = VkDescriptorBufferInfo.callocStack(stack);
                descriptorBI.buffer(uniformBuffers.get(index));
                descriptorBI.offset(0);
                descriptorBI.range(UniformBufferObject.BYTES);

                VkDescriptorImageInfo imageInfo = VkDescriptorImageInfo.callocStack(stack);
                imageInfo.imageLayout(VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
                imageInfo.imageView(textureImageView);
                imageInfo.sampler(textureSampler);

                VkWriteDescriptorSet writeSet1 = VkWriteDescriptorSet.callocStack(stack);
                writeSet1.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
                writeSet1.dstSet(this.descriptorSets[index]);
                writeSet1.dstBinding(0);
                writeSet1.dstArrayElement(0);

                writeSet1.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
                writeSet1.pBufferInfo(VkDescriptorBufferInfo.callocStack(1, stack).put(0, descriptorBI));
                writeSet1.pImageInfo(null);
                writeSet1.pTexelBufferView(null);

                VkWriteDescriptorSet writeSet2 = VkWriteDescriptorSet.callocStack(stack);
                writeSet2.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
                writeSet2.dstSet(this.descriptorSets[index]);
                writeSet2.dstBinding(1);
                writeSet2.dstArrayElement(0);

                writeSet2.descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
                writeSet2.pBufferInfo(null);
                writeSet2.pImageInfo(VkDescriptorImageInfo.callocStack(1, stack).put(0, imageInfo));
                writeSet2.pTexelBufferView(null);

                VK10.vkUpdateDescriptorSets(device, VkWriteDescriptorSet.callocStack(2, stack).put(0, writeSet1).put(1, writeSet2), null);
            }
        }
    }

    void copyBuffer(long source, long dest, long byteSize) {
        try (MemoryStack stack = stackPush()){

            VkCommandBuffer commandBuffer = beginSingleTimeCommands();

            VkBufferCopy copyRegion = VkBufferCopy.callocStack(stack);
            copyRegion.srcOffset(0);
            copyRegion.dstOffset(0);
            copyRegion.size(byteSize);

            VK10.vkCmdCopyBuffer(commandBuffer, source, dest, VkBufferCopy.callocStack(1, stack).put(0, copyRegion));

            endSingleTimeCommands(commandBuffer);
        }
    }

    void transitionImageLayout(long image, int format, int oldLayout, int newLayout, int mipLevels) {
        try (MemoryStack stack = stackPush()){
            VkCommandBuffer commandBuffer = beginSingleTimeCommands();

            VkImageMemoryBarrier barrier = VkImageMemoryBarrier.callocStack(stack);
            barrier.sType(VK10.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
            barrier.oldLayout(oldLayout);
            barrier.newLayout(newLayout);
            barrier.srcQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED);
            barrier.dstQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED);

            barrier.image(image);

            int aspectMask;
            if (newLayout == VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL) {
                aspectMask = VK10.VK_IMAGE_ASPECT_DEPTH_BIT;

                if (hasStencilComponent(format)) {
                    aspectMask |= VK10.VK_IMAGE_ASPECT_STENCIL_BIT;
                }
            } else {
                aspectMask = VK10.VK_IMAGE_ASPECT_COLOR_BIT;
            }

            barrier.subresourceRange(VkImageSubresourceRange.callocStack(stack).set(aspectMask, 0, mipLevels, 0, 1));

            int srcStage, dstStage;

            if (oldLayout == VK10.VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {
                barrier.srcAccessMask(0);
                barrier.dstAccessMask(VK10.VK_ACCESS_TRANSFER_WRITE_BIT);

                srcStage = VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                dstStage = VK10.VK_PIPELINE_STAGE_TRANSFER_BIT;
            } else if (oldLayout == VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL && newLayout == VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {
                barrier.srcAccessMask(VK10.VK_ACCESS_TRANSFER_WRITE_BIT);
                barrier.dstAccessMask(VK10.VK_ACCESS_SHADER_READ_BIT);

                srcStage = VK10.VK_PIPELINE_STAGE_TRANSFER_BIT;
                dstStage = VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
            } else if (oldLayout == VK10.VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL) {
                barrier.srcAccessMask(0);
                barrier.dstAccessMask(VK10.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK10.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);

                srcStage = VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                dstStage = VK10.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT;
            }
            else {
                throw new IllegalArgumentException("Unsupported combination of (oldLayout,newLayout): (" + oldLayout + "," + newLayout + ")");
            }

            VK10.vkCmdPipelineBarrier(commandBuffer, srcStage, dstStage, 0, null, null, VkImageMemoryBarrier.callocStack(1, stack).put(0, barrier));

            endSingleTimeCommands(commandBuffer);
        }
    }

    void copyBufferToImage(long buffer, long image, int width, int height) {
        try (MemoryStack stack = stackPush()){
            VkCommandBuffer commandBuffer = beginSingleTimeCommands();

            VkBufferImageCopy region = VkBufferImageCopy.callocStack(stack);
            region.bufferOffset(0);
            region.bufferRowLength(0);
            region.bufferImageHeight(0);

            region.imageSubresource(VkImageSubresourceLayers.callocStack(stack).set(VK10.VK_IMAGE_ASPECT_COLOR_BIT, 0, 0, 1));

            region.imageOffset(VkOffset3D.callocStack(stack).set(0, 0, 0));
            region.imageExtent(VkExtent3D.callocStack(stack).set(width, height, 1));

            VK10.vkCmdCopyBufferToImage(commandBuffer, buffer, image, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VkBufferImageCopy.callocStack(1, stack).put(0, region));

            endSingleTimeCommands(commandBuffer);
        }
    }

    void createCommandBuffers() {
        try (MemoryStack stack = stackPush()) {
            VkCommandBufferAllocateInfo commandBufferAI = VkCommandBufferAllocateInfo.callocStack(stack);
            commandBufferAI.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            commandBufferAI.commandPool(commandPool);
            commandBufferAI.level(VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            commandBuffers = MemoryUtil.memCallocPointer(swapchainFrameBuffers.capacity());
            commandBufferAI.commandBufferCount(commandBuffers.capacity());
            validate(VK10.vkAllocateCommandBuffers(device, commandBufferAI, commandBuffers));
        }

        for (int index = 0; index < commandBuffers.capacity(); index++) {
            try (MemoryStack stack = stackPush()) {
                VkCommandBufferBeginInfo commandBufferBI = VkCommandBufferBeginInfo.callocStack(stack);
                commandBufferBI.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
                commandBufferBI.flags(VK10.VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT);
                commandBufferBI.pInheritanceInfo(null);

                VkCommandBuffer commandBuffer = new VkCommandBuffer(commandBuffers.get(index), device);

                validate(VK10.vkBeginCommandBuffer(commandBuffer, commandBufferBI));

                VkRenderPassBeginInfo renderPassBI = VkRenderPassBeginInfo.callocStack(stack);
                renderPassBI.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
                renderPassBI.renderPass(renderPass);
                renderPassBI.framebuffer(swapchainFrameBuffers.get(index));
                renderPassBI.renderArea().offset(VkOffset2D.callocStack(stack).set(0, 0));
                renderPassBI.renderArea().extent(swapchainImageExtent);

                VkClearColorValue clearColor = VkClearColorValue.callocStack(stack);
                clearColor.float32(stack.floats(0f, 1f, 0f, 1f));
                VkClearDepthStencilValue clearDepth = VkClearDepthStencilValue.callocStack(stack);
                clearDepth.set(1f, 0);
                renderPassBI.pClearValues(VkClearValue.callocStack(2, stack)
                        .put(0, VkClearValue.callocStack(stack).color(clearColor)).put(1, VkClearValue.callocStack(stack).depthStencil(clearDepth)));

                VK10.vkCmdBeginRenderPass(commandBuffer, renderPassBI, VK10.VK_SUBPASS_CONTENTS_INLINE);

                VK10.vkCmdBindPipeline(commandBuffer, VK10.VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);

                VK10.vkCmdBindDescriptorSets(commandBuffer, VK10.VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayout, 0, stack.longs(descriptorSets[index]), null);

                // TODO It is recommended to use the offsets to store multiple models in the same buffer
                VK10.vkCmdBindVertexBuffers(commandBuffer, 0, stack.longs(vertexBuffer), stack.longs(0));

                VK10.vkCmdBindIndexBuffer(commandBuffer, indexBuffer, 0, VK10.VK_INDEX_TYPE_UINT16);

                VK10.vkCmdDrawIndexed(commandBuffer, INDICES.length, 1, 0, 0, 0);

                VK10.vkCmdEndRenderPass(commandBuffer);

                validate(VK10.vkEndCommandBuffer(commandBuffer));
            }
        }
    }

    void createSyncObjects() {
        try (MemoryStack stack = stackPush()) {

            VkSemaphoreCreateInfo semaphoreCI = VkSemaphoreCreateInfo.callocStack(stack);
            semaphoreCI.sType(VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

            VkFenceCreateInfo fenceCI = VkFenceCreateInfo.callocStack(stack);
            fenceCI.sType(VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
            fenceCI.flags(VK10.VK_FENCE_CREATE_SIGNALED_BIT);

            LongBuffer resultBuffer = stack.callocLong(1);
            for (int index = 0; index < MAX_FRAMES_IN_FLIGHT; index++) {
                validate(VK10.vkCreateSemaphore(device, semaphoreCI, null, resultBuffer));
                imageAvailableSemaphores[index] = resultBuffer.get(0);
                validate(VK10.vkCreateSemaphore(device, semaphoreCI, null, resultBuffer));
                renderFinishedSemaphores[index] = resultBuffer.get(0);
                validate(VK10.vkCreateFence(device, fenceCI, null, resultBuffer));
                inFlightFences[index] = resultBuffer.get(0);
            }
        }
    }

    long createShaderModule(byte[] code) {
        final long shaderModule;
        try (MemoryStack stack = stackPush()) {
            VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.callocStack(stack);
            createInfo.sType(VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
            createInfo.pCode(stack.bytes(code));
            LongBuffer shaderModuleBuffer = stack.callocLong(1);
            validate(VK10.vkCreateShaderModule(device, createInfo, null, shaderModuleBuffer));
            shaderModule = shaderModuleBuffer.get(0);
        }
        return shaderModule;
    }

    static class UniformBufferObject {

        static final int MATRIX_COUNT = 3;
        static final int MATRIX_FLOATS = 16;

        static final int FLOATS = MATRIX_COUNT * MATRIX_FLOATS;
        static final int BYTES = FLOATS * Float.BYTES;

        Matrix4f model;
        Matrix4f view;
        Matrix4f proj;

        UniformBufferObject(){

        }

        void put(FloatBuffer dest, int offset) {
            model.get(offset, dest);
            view.get(offset + MATRIX_FLOATS, dest);
            proj.get(offset + 2 * MATRIX_FLOATS, dest);

            // TODO Make sure that you apply all alignment rules of Vulkan! (It is now, but don't forget it later)
        }
    }

    static class Vertex {

        static final int FLOATS = 3 + 3 + 2;
        static final int BYTES = FLOATS * 4;

        float x,y,z;
        float red,green,blue;
        float u,v;

        Vertex(float x, float y, float z, float red, float green, float blue, float u, float v){
            this.x = x;
            this.y = y;
            this.z = z;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.u = u;
            this.v = v;
        }

        void put(FloatBuffer dest, int index) {
            dest.put(index, x);
            dest.put(index + 1, y);
            dest.put(index + 2, z);
            dest.put(index + 3, red);
            dest.put(index + 4, green);
            dest.put(index + 5, blue);
            dest.put(index + 6, u);
            dest.put(index + 7, v);
        }

        static VkVertexInputBindingDescription.Buffer getBindingDescription(MemoryStack stack) {
            VkVertexInputBindingDescription description = VkVertexInputBindingDescription.callocStack(stack);
            description.binding(0);
            description.stride(Vertex.BYTES);
            description.inputRate(VK10.VK_VERTEX_INPUT_RATE_VERTEX);
            return VkVertexInputBindingDescription.callocStack(1, stack).put(0, description);
        }

        static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(MemoryStack stack) {
            VkVertexInputAttributeDescription position = VkVertexInputAttributeDescription.callocStack(stack);
            position.binding(0);
            position.location(0);
            position.format(VK10.VK_FORMAT_R32G32B32_SFLOAT);
            position.offset(0);

            VkVertexInputAttributeDescription color = VkVertexInputAttributeDescription.callocStack(stack);
            color.binding(0);
            color.location(1);
            color.format(VK10.VK_FORMAT_R32G32B32_SFLOAT);
            color.offset(3 * 4);

            VkVertexInputAttributeDescription textureCoords = VkVertexInputAttributeDescription.callocStack(stack);
            textureCoords.binding(0);
            textureCoords.location(2);
            textureCoords.format(VK10.VK_FORMAT_R32G32_SFLOAT);
            textureCoords.offset(3 * 4 + 3 * 4);

            VkVertexInputAttributeDescription.Buffer descriptions = VkVertexInputAttributeDescription.callocStack(3, stack);
            descriptions.put(0, position);
            descriptions.put(1, color);
            descriptions.put(2, textureCoords);

            return descriptions;
        }
    }

    static final Vertex[] VERTICES = {
            new Vertex(-0.5f,-0.5f,0f, 1f,0f,0f, 1f,0f),
            new Vertex(-0.5f,0.5f,0f, 1f,1f,1f, 1f,1f),
            new Vertex(0.5f,0.5f,0f, 0f,0f,1f, 0f,1f),
            new Vertex(0.5f,-0.5f,0f, 0f,1f,0f, 0f,0f),

            new Vertex(-0.5f,-0.5f,-0.5f, 1f,0f,0f, 1f,0f),
            new Vertex(-0.5f,0.5f,-0.5f, 1f,1f,1f, 1f,1f),
            new Vertex(0.5f,0.5f,-0.5f, 0f,0f,1f, 0f,1f),
            new Vertex(0.5f,-0.5f,-0.5f, 0f,1f,0f, 0f,0f),
    };

    static final short[] INDICES = {
            0,1,2,
            2,3,0,

            4,5,6,
            6,7,4
    };

    static final int INDEX_TYPE_SIZE = 2;

    byte[] readFile(String resourceName) {
        try {
            URL url = TriangleTest.class.getClassLoader().getResource(resourceName);
            URLConnection resourceConnection = url.openConnection();
            byte[] content = new byte[resourceConnection.getContentLength()];
            System.out.println("resource content length is " + content.length);
            DataInputStream stream = new DataInputStream(resourceConnection.getInputStream());
            stream.readFully(content);
            stream.close();
            return content;
        } catch (IOException ioex) {
            throw new Error(ioex);
        }
    }

    void createLogicalDevice() {
        try (MemoryStack stack = stackPush()) {
            QueueFamilyIndices queueFamilies = findQueueFamilies(stack, physicalDevice);

            Set<Integer> uniqueQueueFamilyIndices = new HashSet<>(2);
            uniqueQueueFamilyIndices.add(queueFamilies.graphicsFamily);
            uniqueQueueFamilyIndices.add(queueFamilies.presentFamily);

            VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo
                    .callocStack(uniqueQueueFamilyIndices.size(), stack);

            {
                FloatBuffer priorities = stack.floats(1f);

                for (Integer queueFamilyIndex : uniqueQueueFamilyIndices) {
                    VkDeviceQueueCreateInfo queueCreateInfo = VkDeviceQueueCreateInfo.callocStack(stack);
                    queueCreateInfo.sType(VK10.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
                    queueCreateInfo.queueFamilyIndex(queueFamilyIndex);
                    VkDeviceQueueCreateInfo.nqueueCount(queueCreateInfo.address(), 1);
                    queueCreateInfo.pQueuePriorities(priorities);
                    queueCreateInfos.put(queueCreateInfo);
                }
            }

            queueCreateInfos.flip();

            VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.callocStack(stack);
            features.samplerAnisotropy(true);

            VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.callocStack(stack);
            createInfo.sType(VK10.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO);
            createInfo.pQueueCreateInfos(queueCreateInfos);
            createInfo.pEnabledFeatures(features);
            {
                String[] extensionNames = getRequiredDeviceExtensions();
                PointerBuffer extensionsBuffer = stack.callocPointer(extensionNames.length);
                for (int index = 0; index < extensionNames.length; index++) {
                    extensionsBuffer.put(index, stack.UTF8(extensionNames[index]));
                }
                createInfo.ppEnabledExtensionNames(extensionsBuffer);
            }
            createInfo.ppEnabledLayerNames(getLayersToEnable(stack));

            {
                PointerBuffer devicePointer = stack.callocPointer(1);
                validate(VK10.vkCreateDevice(physicalDevice, createInfo, null, devicePointer));
                device = new VkDevice(devicePointer.get(0), physicalDevice, createInfo);
            }

            System.out.println("The logical device has become " + device);

            {
                PointerBuffer queuePointer = stack.callocPointer(1);
                VK10.vkGetDeviceQueue(device, queueFamilies.graphicsFamily, 0, queuePointer);
                graphicsQueue = new VkQueue(queuePointer.get(0), device);
                VK10.vkGetDeviceQueue(device, queueFamilies.presentFamily, 0, queuePointer);
                presentQueue = new VkQueue(queuePointer.get(0), device);
            }

            System.out.println("The graphics queue has become " + graphicsQueue + " and the present queue has become "
                    + presentQueue);
        }
    }

    int findMemoryType(int typeFilter, int properties) {
        try (MemoryStack stack = stackPush()) {
            VkPhysicalDeviceMemoryProperties memProps = VkPhysicalDeviceMemoryProperties.callocStack(stack);
            VK10.vkGetPhysicalDeviceMemoryProperties(physicalDevice, memProps);
            int memoryTypeCount = memProps.memoryTypeCount();
            for (int index = 0; index < memoryTypeCount; index++) {
                if ((typeFilter & (1 << index)) != 0) {
                    VkMemoryType currentType = memProps.memoryTypes(index);
                    if ((currentType.propertyFlags() & properties) == properties) {
                        return index;
                    }
                }
            }

            throw new UnsupportedOperationException("No suitable memory type found");
        }
    }

    void validate(int errorCode) {
        if (errorCode != VK10.VK_SUCCESS)
            throw new VulkanException(errorCode);
    }

    void mainLoop() {

        long frameCounter = 0;
        long startTime = System.currentTimeMillis();
        while (!GLFW.glfwWindowShouldClose(window)) {
            GLFW.glfwPollEvents();
            drawFrame();
            frameCounter++;
        }

        if (frameCounter > 0)
            System.out.println("Average fps was " + 1000 * frameCounter / (System.currentTimeMillis() - startTime));
        else
            System.out.println("Not a single frame was finished, so couldn't determine fps");
        VK10.vkDeviceWaitIdle(device);
    }

    void drawFrame() {
        try (MemoryStack stack = stackPush()) {
            next("drawFrame wait for fences");
            LongBuffer fencesBuffer = stack.longs(inFlightFences[currentFrame]);
            validate(VK10.vkWaitForFences(device, fencesBuffer, true, UINT_MAX));

            next("drawFrame acquire next image");
            IntBuffer pImageIndex = stack.callocInt(1);
            int acquireImageResult = KHRSwapchain.vkAcquireNextImageKHR(device, swapchain, UINT_MAX,
                    imageAvailableSemaphores[currentFrame], VK_NULL_HANDLE, pImageIndex);

            // If the result is SUBOPTIMAL, we will finish the drawing of this frame
            // If so, the swapchain will be recreated at the end of this method
            if (acquireImageResult == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
                recreateSwapchain();
                return;
            } else if (acquireImageResult != VK10.VK_SUCCESS && acquireImageResult != KHRSwapchain.VK_SUBOPTIMAL_KHR) {
                throw new VulkanException(acquireImageResult);
            }

            LongBuffer signalSemaphores = stack.longs(renderFinishedSemaphores[currentFrame]);

            int imageIndex = pImageIndex.get(0);
            updateUniformBuffer(stack, imageIndex);

            next("drawFrame submit info");
            VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack);
            submitInfo.sType(VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.waitSemaphoreCount(1);
            submitInfo.pWaitSemaphores(stack.longs(imageAvailableSemaphores[currentFrame]));
            submitInfo.pWaitDstStageMask(stack.ints(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));

            submitInfo.pCommandBuffers(stack.pointers(commandBuffers.get(imageIndex)));

            submitInfo.pSignalSemaphores(signalSemaphores);

            next("drawFrame reset fences");
            validate(VK10.vkResetFences(device, fencesBuffer));
            next("drawFrame queue submit");
            validate(VK10.vkQueueSubmit(graphicsQueue, submitInfo, inFlightFences[currentFrame]));

            next("drawFrame presentInfo");
            VkPresentInfoKHR presentInfo = VkPresentInfoKHR.callocStack(stack);
            presentInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
            presentInfo.pWaitSemaphores(signalSemaphores);

            presentInfo.swapchainCount(1);
            presentInfo.pSwapchains(stack.longs(swapchain));
            presentInfo.pImageIndices(pImageIndex);

            presentInfo.pResults(null);

            next("drawFrame queue present");
            int queuePresentResult = KHRSwapchain.vkQueuePresentKHR(presentQueue, presentInfo);
            if (queuePresentResult == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || queuePresentResult == KHRSwapchain.VK_SUBOPTIMAL_KHR || framebufferResized) {
                next("drawFrame recreate the swapchain...");
                framebufferResized = false;
                recreateSwapchain();
            } else if (queuePresentResult != VK10.VK_SUCCESS) {
                throw new VulkanException(queuePresentResult);
            }
            Performance.end();

            currentFrame++;
            if (currentFrame >= MAX_FRAMES_IN_FLIGHT)
                currentFrame = 0;
        }
    }

    // I'm not sure I like this kind of timings...
    static long startTime = System.nanoTime();

    void updateUniformBuffer(MemoryStack stack, int imageIndex) {
        long currentTime = System.nanoTime();
        long elapsed = currentTime - startTime;
        double seconds = elapsed / 1000000000.0;

        UniformBufferObject ubo = new UniformBufferObject();
        ubo.model = new Matrix4f().identity().rotate((float) (seconds * 0.5 * Math.PI), new Vector3f(0f, 0f, 1f));
        ubo.view = new Matrix4f().lookAt(new Vector3f(2f, 2f, 2f), new Vector3f(), new Vector3f(0f, 0f, 1f));

        // Scale because Vulkan is not the same as OpenGL, that's also why the true at the end is needed
        ubo.proj = new Matrix4f().scale(1, -1, 1).perspective((float) (0.15 * Math.PI),
                swapchainImageExtent.width() / (float) swapchainImageExtent.height(), 0.1f, 10f, true);

        PointerBuffer ppData = stack.callocPointer(1);
        validate(VK10.vkMapMemory(device, uniformBuffersMemory.get(imageIndex), 0, UniformBufferObject.BYTES, 0, ppData));

        FloatBuffer destBuffer = MemoryUtil.memByteBuffer(ppData.get(0), UniformBufferObject.FLOATS).asFloatBuffer();
        ubo.put(destBuffer, 0);

        VK10.vkUnmapMemory(device, uniformBuffersMemory.get(imageIndex));

        //startTime = currentTime;
    }

    void cleanupSwapchain() {

        VK10.vkDestroyImageView(device, depthImageView, null);
        VK10.vkDestroyImage(device, depthImage, null);
        VK10.vkFreeMemory(device, depthImageMemory, null);

        for (int index = 0; index < swapchainFrameBuffers.capacity(); index++)
            VK10.vkDestroyFramebuffer(device, swapchainFrameBuffers.get(index), null);

        VK10.vkFreeCommandBuffers(device, commandPool, commandBuffers);

        MemoryUtil.memFree(commandBuffers);
        MemoryUtil.memFree(swapchainFrameBuffers);

        VK10.vkDestroyPipeline(device, graphicsPipeline, null);
        VK10.vkDestroyPipelineLayout(device, pipelineLayout, null);

        VK10.vkDestroyRenderPass(device, renderPass, null);

        for (int index = 0; index < swapchainImageViews.capacity(); index++)
            VK10.vkDestroyImageView(device, swapchainImageViews.get(index), null);

        for (int index = 0; index < swapchainImageViews.capacity(); index++) {
            VK10.vkDestroyBuffer(device, uniformBuffers.get(index), null);
            VK10.vkFreeMemory(device, uniformBuffersMemory.get(index), null);
        }

        VK10.vkDestroyDescriptorPool(device, descriptorPool, null);

        MemoryUtil.memFree(uniformBuffers);
        MemoryUtil.memFree(uniformBuffersMemory);
        MemoryUtil.memFree(swapchainImageViews);
        swapchainImageExtent.free();

        KHRSwapchain.vkDestroySwapchainKHR(device, swapchain, null);
    }

    void recreateSwapchain() {
        try (MemoryStack stack = stackPush()){

            // Notice that the initial values of 0 are crucial for the loop to begin!
            IntBuffer widthBuffer = stack.ints(0);
            IntBuffer heightBuffer = stack.ints(0);
            while (widthBuffer.get(0) == 0 || heightBuffer.get(0) == 0) {
                GLFW.glfwGetFramebufferSize(window, widthBuffer, heightBuffer);
                GLFW.glfwWaitEvents();
            }
        }

        VK10.vkDeviceWaitIdle(device);
        cleanupSwapchain();

        createSwapchain();
        createImageViews();
        createRenderPass();
        createGraphicsPipeline();
        createDepthResources();
        createFramebuffers();
        createUniformBuffers();
        createDescriptorPool();
        createDescriptorSets();
        createCommandBuffers();
    }

    void cleanUp() {
        cleanupSwapchain();

        VK10.vkDestroySampler(device, textureSampler, null);
        VK10.vkDestroyImageView(device, textureImageView, null);
        VK10.vkDestroyImage(device, textureImage, null);
        VK10.vkFreeMemory(device, textureImageMemory, null);

        VK10.vkDestroyDescriptorSetLayout(device, descriptorSetLayout, null);

        VK10.vkDestroyBuffer(device, vertexBuffer, null);
        VK10.vkFreeMemory(device, vertexBufferMemory, null);

        VK10.vkDestroyBuffer(device, indexBuffer, null);
        VK10.vkFreeMemory(device, indexBufferMemory, null);

        MemoryUtil.memFree(swapchainImages);

        for (int index = 0; index < MAX_FRAMES_IN_FLIGHT; index++) {
            VK10.vkDestroySemaphore(device, renderFinishedSemaphores[index], null);
            VK10.vkDestroySemaphore(device, imageAvailableSemaphores[index], null);
            VK10.vkDestroyFence(device, inFlightFences[index], null);
        }

        VK10.vkDestroyCommandPool(device, commandPool, null);

        VK10.vkDestroyDevice(device, null);

        KHRSurface.vkDestroySurfaceKHR(instance, surface, null);

        if (DEBUG)
            EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, null);

        VK10.vkDestroyInstance(instance, null);

        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();

        Performance.print((String description, long duration) -> {
            return !description.startsWith("drawFrame");
        });
    }

    static class VulkanException extends RuntimeException {

        private static final long serialVersionUID = -4545646884008430546L;

        VulkanException(int errorCode) {
            super("Vulkan error code: " + errorCode);
        }
    }
}
%Part1  read the colored image, resize it 
image_colored = imread('myimage2.png');
image_colored = imresize(image_colored,0.1);

%Part2  take the green channel
image = image_colored(:,:,2)
imwrite(image,'image_gray.jpg');

%Part5  define a sigma vector, to get edge detection output within one time
sigma = [0.5 1 2];
for n = 1:size(sigma,2)
    
    %Part3  use gray image, filter with Gaussian and then take local differences
    filter = fspecial('gaussian',[3 3],sigma(n));
    image_gaussian = conv2(image,filter,'same');
    imwrite(uint8(image_gaussian),strcat(num2str(sigma(n)),'_gaussian.jpg'));
    diff_x = conv2(image_gaussian,[1/2,0,-1/2],'same');
    diff_y = conv2(image_gaussian,[-1/2;0;1/2],'same');
    imwrite(uint8(diff_x),strcat(num2str(sigma(n)),'_localdiffx.jpg'));
    imwrite(uint8(diff_y),strcat(num2str(sigma(n)),'_localdiffy.jpg'));
    
    %Part4  use func 'gradient' to compute gradient, give a binary image with
    %threshold
    threshold = 10;
    [Fx, Fy] = gradient(double(image_gaussian));
    magnitude = sqrt(Fx.^2+Fy.^2);
    for i = 1:size(magnitude,1)
        for j = 1:size(magnitude,2)
            if magnitude(i,j) > threshold
                magnitude(i,j) = 255;
            else
                magnitude(i,j) = 0;
            end
        end
    end
    %Part4  compute gradient orientation
    orientation=atan2(Fy,Fx);
    imwrite(magnitude,strcat(num2str(sigma(n)),'_magnitude.jpg'));
    imwrite(orientation,strcat(num2str(sigma(n)),'_orientation.jpg'));
    
    %builtin func, returns gradient magnitude and orientation
    %used for result checking
    %[GX, GY] = imgradient(image); 
end
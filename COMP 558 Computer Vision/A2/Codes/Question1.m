%read the image, take the blue channel
image = imread('james.jpg');
image = image(:,:,3);

%blur the image with a series of gaussian
sigma = [4 8 12 16]
image_gaussian = cell(4,1)
for n = 1:size(sigma,2)
    image_gaussian{n} = imgaussfilt(image, sigma(n));
    imwrite(uint8(image_gaussian{n}),strcat('Q1_gaussian_',num2str(sigma(n)),'.jpg'));
end

%blur the image with my implemention of heat equation
image_heatequation = cell(4,1)
for n = 1:size(sigma,2)
    t = 0.5*sigma(n)^2;
    image_heatequation{n} = heatEquation(image, t);
    imwrite(uint8(image_heatequation{n}),strcat('Q1_heatequation_',num2str(t),'.jpg'));
end

%compare two set of images numerically
for n = 1:size(sigma,2)
    difference = uint8(image_heatequation{n}) - uint8(image_gaussian{n});
    difference(find(difference)) = 255;
    imwrite(uint8(difference),strcat('Q1_difference_',num2str(sigma(n)),'.jpg'));
end

%Run heat equation t times
function image_heatequation = heatEquation(image, t)
for n = 1:t
    [Fx, Fy] = gradient(double(image));
    [Fxx, Fxy] = gradient(double(Fx));
    [Fyx, Fyy] = gradient(double(Fy));
    image_heatequation = double(image) + double(Fxx) + double(Fyy);
    image = image_heatequation;
end
end
